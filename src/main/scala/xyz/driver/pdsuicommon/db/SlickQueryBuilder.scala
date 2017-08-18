package xyz.driver.pdsuicommon.db

import java.sql.PreparedStatement
import java.time.LocalDateTime

import slick.driver.JdbcProfile
import slick.jdbc.{PositionedParameters, SQLActionBuilder, SetParameter}
import xyz.driver.pdsuicommon.db.Sorting.{Dimension, Sequential}
import xyz.driver.pdsuicommon.db.SortingOrder.{Ascending, Descending}

import scala.concurrent.{ExecutionContext, Future}

object SlickQueryBuilder {

  type Runner[T] = SlickQueryBuilderParameters => Future[Seq[T]]

  type CountResult = Future[(Int, Option[LocalDateTime])]

  type CountRunner = SlickQueryBuilderParameters => CountResult

  /**
    * Binder for PreparedStatement
    */
  type Binder = PreparedStatement => PreparedStatement

  final case class TableData(tableName: String,
                             lastUpdateFieldName: Option[String] = None,
                             nullableFields: Set[String] = Set.empty)

  val AllFields = Set("*")

  implicit class SQLActionBuilderConcat(a: SQLActionBuilder) {
    def concat(b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(a.queryParts ++ b.queryParts, new SetParameter[Unit] {
        def apply(p: Unit, pp: PositionedParameters): Unit = {
          a.unitPConv.apply(p, pp)
          b.unitPConv.apply(p, pp)
        }
      })
    }
  }
}

final case class SlickTableLink(keyColumnName: String, foreignTableName: String, foreignKeyColumnName: String)

object SlickQueryBuilderParameters {
  val AllFields = Set("*")
}

sealed trait SlickQueryBuilderParameters {
  import SlickQueryBuilder._

  def tableData: SlickQueryBuilder.TableData
  def links: Map[String, SlickTableLink]
  def filter: SearchFilterExpr
  def sorting: Sorting
  def pagination: Option[Pagination]

  def findLink(tableName: String): SlickTableLink = links.get(tableName) match {
    case None       => throw new IllegalArgumentException(s"Cannot find a link for `$tableName`")
    case Some(link) => link
  }

  def toSql(countQuery: Boolean = false)(implicit profile: JdbcProfile): SQLActionBuilder = {
    toSql(countQuery, QueryBuilderParameters.AllFields)
  }

  def toSql(countQuery: Boolean, fields: Set[String])(implicit profile: JdbcProfile): SQLActionBuilder = {
    import profile.api._
    val escapedTableName = tableData.tableName
    val fieldsSql: String = if (countQuery) {
      val suffix: String = tableData.lastUpdateFieldName match {
        case Some(lastUpdateField) => s", max($escapedTableName.$lastUpdateField)"
        case None                  => ""
      }
      "count(*)" + suffix
    } else {
      if (fields == SlickQueryBuilderParameters.AllFields) {
        s"$escapedTableName.*"
      } else {
        fields
          .map { field =>
            s"$escapedTableName.$field"
          }
          .mkString(", ")
      }
    }
    val where   = filterToSql(escapedTableName, filter)
    val orderBy = sortingToSql(escapedTableName, sorting)

    val limitSql = limitToSql()

    val sql = sql"select #$fieldsSql from #$escapedTableName"

    val filtersTableLinks: Seq[SlickTableLink] = {
      import SearchFilterExpr._
      def aux(expr: SearchFilterExpr): Seq[SlickTableLink] = expr match {
        case Atom.TableName(tableName) => List(findLink(tableName))
        case Intersection(xs)          => xs.flatMap(aux)
        case Union(xs)                 => xs.flatMap(aux)
        case _                         => Nil
      }
      aux(filter)
    }

    val sortingTableLinks: Seq[SlickTableLink] = Sorting.collect(sorting) {
      case Dimension(Some(foreignTableName), _, _) => findLink(foreignTableName)
    }

    // Combine links from sorting and filter without duplicates
    val foreignTableLinks = (filtersTableLinks ++ sortingTableLinks).distinct

    def fkSql(fkLinksSql: SQLActionBuilder, tableLinks: Seq[SlickTableLink]): SQLActionBuilder = {
      if (tableLinks.nonEmpty) {
        tableLinks.head match {
          case SlickTableLink(keyColumnName, foreignTableName, foreignKeyColumnName) =>
            fkSql(
              fkLinksSql concat sql""" inner join #$foreignTableName
             on #$escapedTableName.#$keyColumnName = #$foreignTableName.#$foreignKeyColumnName""",
              tableLinks.tail
            )
        }
      } else fkLinksSql
    }
    val foreignTableLinksSql = fkSql(sql"", foreignTableLinks)

    val whereSql = if (where.toString.nonEmpty) {
      sql" where " concat where
    } else sql""

    val orderSql = if (orderBy.toString.nonEmpty && !countQuery) {
      sql" order by" concat orderBy
    } else sql""

    val limSql = if (limitSql.toString.nonEmpty && !countQuery) {
      limitSql
    } else sql""

    sql concat foreignTableLinksSql concat whereSql concat orderSql concat limSql
  }

  /**
    * Converts filter expression to SQL expression.
    *
    * @return Returns SQL string and list of values for binding in prepared statement.
    */
  protected def filterToSql(escapedTableName: String, filter: SearchFilterExpr)(
          implicit profile: JdbcProfile): SQLActionBuilder = {
    import SearchFilterBinaryOperation._
    import SearchFilterExpr._
    import profile.api._

    def isNull(string: AnyRef) = Option(string).isEmpty || string.toString.toLowerCase == "null"

    def escapeDimension(dimension: SearchFilterExpr.Dimension) = {
      val tableName = escapedTableName
      s"$tableName.${dimension.name}"
    }

    def filterToSqlMultiple(operands: Seq[SearchFilterExpr]) = operands.collect {
      case x if !SearchFilterExpr.isEmpty(x) => filterToSql(escapedTableName, x)
    }

    def multipleSqlToAction(first: Boolean,
                            op: String,
                            conditions: Seq[SQLActionBuilder],
                            sql: SQLActionBuilder): SQLActionBuilder = {
      if (conditions.nonEmpty) {
        val condition = conditions.head
        if (first) {
          multipleSqlToAction(false, op, conditions.tail, condition)
        } else {
          multipleSqlToAction(false, op, conditions.tail, sql concat sql" #${op} " concat condition)
        }
      } else sql
    }

    filter match {
      case x if isEmpty(x) =>
        sql""

      case AllowAll =>
        sql"1"

      case DenyAll =>
        sql"0"

      case Atom.Binary(dimension, Eq, value) if isNull(value) =>
        sql"#${escapeDimension(dimension)} is NULL"

      case Atom.Binary(dimension, NotEq, value) if isNull(value) =>
        sql"#${escapeDimension(dimension)} is not NULL"

      case Atom.Binary(dimension, NotEq, value) if tableData.nullableFields.contains(dimension.name) =>
        // In MySQL NULL <> Any === NULL
        // So, to handle NotEq for nullable fields we need to use more complex SQL expression.
        // http://dev.mysql.com/doc/refman/5.7/en/working-with-null.html
        val escapedColumn = escapeDimension(dimension)
        sql"(#${escapedColumn} is null or #${escapedColumn} != ${value.toString})"

      case Atom.Binary(dimension, op, value) =>
        val operator = op match {
          case Eq    => sql"="
          case NotEq => sql"!="
          case Like  => sql"like"
          case Gt    => sql">"
          case GtEq  => sql">="
          case Lt    => sql"<"
          case LtEq  => sql"<="
        }
        sql"#${escapeDimension(dimension)}" concat operator concat sql"""${value.toString}"""

      case Atom.NAry(dimension, op, values) =>
        val sqlOp = op match {
          case SearchFilterNAryOperation.In    => sql"in"
          case SearchFilterNAryOperation.NotIn => sql"not in"
        }

        val formattedValues = if (values.nonEmpty) {
          sql"${values.mkString(",")}"
        } else sql"NULL"
        sql"#${escapeDimension(dimension)}" concat sqlOp concat formattedValues

      case Intersection(operands) =>
        val filter = multipleSqlToAction(true, "and", filterToSqlMultiple(operands), sql"")
        sql"(" concat filter concat sql")"

      case Union(operands) =>
        val filter = multipleSqlToAction(true, "or", filterToSqlMultiple(operands), sql"")
        sql"(" concat filter concat sql")"
    }
  }

  protected def limitToSql()(implicit profile: JdbcProfile): SQLActionBuilder

  /**
    * @param escapedMainTableName Should be escaped
    */
  protected def sortingToSql(escapedMainTableName: String, sorting: Sorting)(
          implicit profile: JdbcProfile): SQLActionBuilder = {
    import profile.api._
    sorting match {
      case Dimension(optSortingTableName, field, order) =>
        val sortingTableName = optSortingTableName.getOrElse(escapedMainTableName)
        val fullName         = s"$sortingTableName.$field"

        sql"#$fullName ${orderToSql(order)}"

      case Sequential(xs) =>
        sql"${xs.map(sortingToSql(escapedMainTableName, _)).mkString(", ")}"
    }
  }

  protected def orderToSql(x: SortingOrder): String = x match {
    case Ascending  => "asc"
    case Descending => "desc"
  }

  protected def binder(bindings: List[AnyRef])(bind: PreparedStatement): PreparedStatement = {
    bindings.zipWithIndex.foreach {
      case (binding, index) =>
        bind.setObject(index + 1, binding)
    }

    bind
  }

}

final case class SlickPostgresQueryBuilderParameters(tableData: SlickQueryBuilder.TableData,
                                                     links: Map[String, SlickTableLink] = Map.empty,
                                                     filter: SearchFilterExpr = SearchFilterExpr.Empty,
                                                     sorting: Sorting = Sorting.Empty,
                                                     pagination: Option[Pagination] = None)
    extends SlickQueryBuilderParameters {

  def limitToSql()(implicit profile: JdbcProfile): SQLActionBuilder = {
    import profile.api._
    pagination.map { pagination =>
      val startFrom = (pagination.pageNumber - 1) * pagination.pageSize
      sql"limit ${pagination.pageSize} OFFSET $startFrom"
    } getOrElse (sql"")
  }

}

/**
  * @param links Links to another tables grouped by foreignTableName
  */
final case class SlickMysqlQueryBuilderParameters(tableData: SlickQueryBuilder.TableData,
                                                  links: Map[String, SlickTableLink] = Map.empty,
                                                  filter: SearchFilterExpr = SearchFilterExpr.Empty,
                                                  sorting: Sorting = Sorting.Empty,
                                                  pagination: Option[Pagination] = None)
    extends SlickQueryBuilderParameters {

  def limitToSql()(implicit profile: JdbcProfile): SQLActionBuilder = {
    import profile.api._
    pagination
      .map { pagination =>
        val startFrom = (pagination.pageNumber - 1) * pagination.pageSize
        sql"limit $startFrom, ${pagination.pageSize}"
      }
      .getOrElse(sql"")
  }

}

abstract class SlickQueryBuilder[T](val parameters: SlickQueryBuilderParameters)(
        implicit runner: SlickQueryBuilder.Runner[T],
        countRunner: SlickQueryBuilder.CountRunner) {

  def run()(implicit ec: ExecutionContext): Future[Seq[T]] = runner(parameters)

  def runCount()(implicit ec: ExecutionContext): SlickQueryBuilder.CountResult = countRunner(parameters)

  /**
    * Runs the query and returns total found rows without considering of pagination.
    */
  def runWithCount()(implicit ec: ExecutionContext): Future[(Seq[T], Int, Option[LocalDateTime])] = {
    for {
      all                 <- run
      (total, lastUpdate) <- runCount
    } yield (all, total, lastUpdate)
  }

  def withFilter(newFilter: SearchFilterExpr): SlickQueryBuilder[T]

  def withFilter(filter: Option[SearchFilterExpr]): SlickQueryBuilder[T] = {
    filter.fold(this)(withFilter)
  }

  def resetFilter: SlickQueryBuilder[T] = withFilter(SearchFilterExpr.Empty)

  def withSorting(newSorting: Sorting): SlickQueryBuilder[T]

  def withSorting(sorting: Option[Sorting]): SlickQueryBuilder[T] = {
    sorting.fold(this)(withSorting)
  }

  def resetSorting: SlickQueryBuilder[T] = withSorting(Sorting.Empty)

  def withPagination(newPagination: Pagination): SlickQueryBuilder[T]

  def withPagination(pagination: Option[Pagination]): SlickQueryBuilder[T] = {
    pagination.fold(this)(withPagination)
  }

  def resetPagination: SlickQueryBuilder[T]

}
