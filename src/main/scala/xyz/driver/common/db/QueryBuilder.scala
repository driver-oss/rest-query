package xyz.driver.common.db

import java.sql.PreparedStatement
import java.time.LocalDateTime

import io.getquill.NamingStrategy
import io.getquill.context.sql.idiom.SqlIdiom
import xyz.driver.common.db.Sorting.{Dimension, Sequential}
import xyz.driver.common.db.SortingOrder.{Ascending, Descending}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

object QueryBuilder {

  type Runner[T] = (QueryBuilderParameters) => Future[Seq[T]]

  type CountResult = (Int, Option[LocalDateTime])

  type CountRunner = (QueryBuilderParameters) => Future[CountResult]

  /**
    * Binder for PreparedStatement
    */
  type Binder = PreparedStatement => PreparedStatement

  case class TableData(tableName: String,
                       lastUpdateFieldName: Option[String] = None,
                       nullableFields: Set[String] = Set.empty)

  val AllFields = Set("*")

}

case class TableLink(keyColumnName: String,
                     foreignTableName: String,
                     foreignKeyColumnName: String)

object QueryBuilderParameters {
  val AllFields = Set("*")
}

sealed trait QueryBuilderParameters {

  def tableData: QueryBuilder.TableData
  def links: Map[String, TableLink]
  def filter: SearchFilterExpr
  def sorting: Sorting
  def pagination: Option[Pagination]

  def findLink(tableName: String): TableLink = links.get(tableName) match {
    case None => throw new IllegalArgumentException(s"Cannot find a link for `$tableName`")
    case Some(link) => link
  }

  def toSql(countQuery: Boolean = false, namingStrategy: NamingStrategy): (String, QueryBuilder.Binder) = {
    toSql(countQuery, QueryBuilderParameters.AllFields, namingStrategy)
  }

  def toSql(countQuery: Boolean,
            fields: Set[String],
            namingStrategy: NamingStrategy): (String, QueryBuilder.Binder) = {
    val escapedTableName = namingStrategy.table(tableData.tableName)
    val fieldsSql: String = if (countQuery) {
      "count(*)" + (tableData.lastUpdateFieldName match {
        case Some(lastUpdateField) => s", max($escapedTableName.${namingStrategy.column(lastUpdateField)})"
        case None => ""
      })
    } else {
      if (fields == QueryBuilderParameters.AllFields) {
        s"$escapedTableName.*"
      } else {
        fields
          .map { field =>
            s"$escapedTableName.${namingStrategy.column(field)}"
          }
          .mkString(", ")
      }
    }
    val (where, bindings) = filterToSql(escapedTableName, filter, namingStrategy)
    val orderBy = sortingToSql(escapedTableName, sorting, namingStrategy)

    val limitSql = limitToSql()

    val sql = new StringBuilder()
    sql.append("select ")
    sql.append(fieldsSql)
    sql.append("\nfrom ")
    sql.append(escapedTableName)

    val filtersTableLinks: Seq[TableLink] = {
      import SearchFilterExpr._
      def aux(expr: SearchFilterExpr): Seq[TableLink] = expr match {
        case Atom.TableName(tableName) => List(findLink(tableName))
        case Intersection(xs) => xs.flatMap(aux)
        case Union(xs) => xs.flatMap(aux)
        case _ => Nil
      }
      aux(filter)
    }

    val sortingTableLinks: Seq[TableLink] = Sorting.collect(sorting) {
      case Dimension(Some(foreignTableName), _, _) => findLink(foreignTableName)
    }

    // Combine links from sorting and filter without duplicates
    val foreignTableLinks = (filtersTableLinks ++ sortingTableLinks).distinct

    foreignTableLinks.foreach {
      case TableLink(keyColumnName, foreignTableName, foreignKeyColumnName) =>
        val escapedForeignTableName = namingStrategy.table(foreignTableName)

        sql.append("\ninner join ")
        sql.append(escapedForeignTableName)
        sql.append(" on ")

        sql.append(escapedTableName)
        sql.append('.')
        sql.append(namingStrategy.column(keyColumnName))

        sql.append(" = ")

        sql.append(escapedForeignTableName)
        sql.append('.')
        sql.append(namingStrategy.column(foreignKeyColumnName))
    }

    if (where.nonEmpty) {
      sql.append("\nwhere ")
      sql.append(where)
    }

    if (orderBy.nonEmpty && !countQuery) {
      sql.append("\norder by ")
      sql.append(orderBy)
    }

    if (limitSql.nonEmpty && !countQuery) {
      sql.append("\n")
      sql.append(limitSql)
    }

    (sql.toString, binder(bindings))
  }

  /**
    * Converts filter expression to SQL expression.
    *
    * @return Returns SQL string and list of values for binding in prepared statement.
    */
  protected def filterToSql(escapedTableName: String,
                            filter: SearchFilterExpr,
                            namingStrategy: NamingStrategy): (String, List[AnyRef]) = {
    import SearchFilterBinaryOperation._
    import SearchFilterExpr._

    def isNull(string: AnyRef) = Option(string).isEmpty || string.toString.toLowerCase == "null"

    def placeholder(field: String) = "?"

    def escapeDimension(dimension: SearchFilterExpr.Dimension) = {
      val tableName = dimension.tableName.fold(escapedTableName)(namingStrategy.table)
      s"$tableName.${namingStrategy.column(dimension.name)}"
    }

    def filterToSqlMultiple(operands: Seq[SearchFilterExpr]) = operands.collect {
      case x if !SearchFilterExpr.isEmpty(x) => filterToSql(escapedTableName, x, namingStrategy)
    }

    filter match {
      case x if isEmpty(x) =>
        ("", List.empty)

      case AllowAll =>
        ("1", List.empty)

      case DenyAll =>
        ("0", List.empty)

      case Atom.Binary(dimension, Eq, value) if isNull(value) =>
        (s"${escapeDimension(dimension)} is NULL", List.empty)

      case Atom.Binary(dimension, NotEq, value) if isNull(value) =>
        (s"${escapeDimension(dimension)} is not NULL", List.empty)

      case Atom.Binary(dimension, NotEq, value) if tableData.nullableFields.contains(dimension.name) =>
        // In MySQL NULL <> Any === NULL
        // So, to handle NotEq for nullable fields we need to use more complex SQL expression.
        // http://dev.mysql.com/doc/refman/5.7/en/working-with-null.html
        val escapedColumn = escapeDimension(dimension)
        val sql = s"($escapedColumn is null or $escapedColumn != ${placeholder(dimension.name)})"
        (sql, List(value))

      case Atom.Binary(dimension, op, value) =>
        val operator = op match {
          case Eq => "="
          case NotEq => "!="
          case Like => "like"
          case Gt => ">"
          case GtEq => ">="
          case Lt => "<"
          case LtEq => "<="
        }
        (s"${escapeDimension(dimension)} $operator ${placeholder(dimension.name)}", List(value))

      case Atom.NAry(dimension, op, values) =>
        val sqlOp = op match {
          case SearchFilterNAryOperation.In => "in"
          case SearchFilterNAryOperation.NotIn => "not in"
        }

        val bindings = ListBuffer[AnyRef]()
        val sqlPlaceholder = placeholder(dimension.name)
        val formattedValues = values.map { value =>
          bindings += value
          sqlPlaceholder
        }.mkString(", ")
        (s"${escapeDimension(dimension)} $sqlOp ($formattedValues)", bindings.toList)

      case Intersection(operands) =>
        val (sql, bindings) = filterToSqlMultiple(operands).unzip
        (sql.mkString("(", " and ", ")"), bindings.flatten.toList)

      case Union(operands) =>
        val (sql, bindings) = filterToSqlMultiple(operands).unzip
        (sql.mkString("(", " or ", ")"), bindings.flatten.toList)
    }
  }

  protected def limitToSql(): String

  /**
    * @param escapedMainTableName Should be escaped
    */
  protected def sortingToSql(escapedMainTableName: String,
                             sorting: Sorting,
                             namingStrategy: NamingStrategy): String = {
    sorting match {
      case Dimension(optSortingTableName, field, order) =>
        val sortingTableName = optSortingTableName.map(namingStrategy.table).getOrElse(escapedMainTableName)
        val fullName = s"$sortingTableName.${namingStrategy.column(field)}"

        s"$fullName ${orderToSql(order)}"

      case Sequential(xs) =>
        xs.map(sortingToSql(escapedMainTableName, _, namingStrategy)).mkString(", ")
    }
  }

  protected def orderToSql(x: SortingOrder): String = x match {
    case Ascending => "asc"
    case Descending => "desc"
  }

  protected def binder(bindings: List[AnyRef])
                      (bind: PreparedStatement): PreparedStatement = {
    bindings.zipWithIndex.foreach { case (binding, index) =>
      bind.setObject(index + 1, binding)
    }

    bind
  }

}

case class PostgresQueryBuilderParameters(tableData: QueryBuilder.TableData,
                                          links: Map[String, TableLink] = Map.empty,
                                          filter: SearchFilterExpr = SearchFilterExpr.Empty,
                                          sorting: Sorting = Sorting.Empty,
                                          pagination: Option[Pagination] = None) extends QueryBuilderParameters {

  def limitToSql(): String = {
    pagination.map { pagination =>
      val startFrom = (pagination.pageNumber - 1) * pagination.pageSize
      s"limit ${pagination.pageSize} OFFSET $startFrom"
    } getOrElse ""
  }

}

/**
  * @param links Links to another tables grouped by foreignTableName
  */
case class MysqlQueryBuilderParameters(tableData: QueryBuilder.TableData,
                                       links: Map[String, TableLink] = Map.empty,
                                       filter: SearchFilterExpr = SearchFilterExpr.Empty,
                                       sorting: Sorting = Sorting.Empty,
                                       pagination: Option[Pagination] = None) extends QueryBuilderParameters {

  def limitToSql(): String = pagination.map { pagination =>
    val startFrom = (pagination.pageNumber - 1) * pagination.pageSize
    s"limit $startFrom, ${pagination.pageSize}"
  }.getOrElse("")

}

abstract class QueryBuilder[T, D <: SqlIdiom, N <: NamingStrategy](val parameters: QueryBuilderParameters)
                                                                  (implicit runner: QueryBuilder.Runner[T],
                                                                   countRunner: QueryBuilder.CountRunner,
                                                                   ec: ExecutionContext) {

  def run: Future[Seq[T]] = runner(parameters)

  def runCount: Future[QueryBuilder.CountResult] = countRunner(parameters)

  /**
    * Runs the query and returns total found rows without considering of pagination.
    */
  def runWithCount: Future[(Seq[T], Int, Option[LocalDateTime])] = {
    val countFuture = runCount
    val selectAllFuture = run
    for {
      (total, lastUpdate) <- countFuture
      all <- selectAllFuture
    } yield (all, total, lastUpdate)
  }

  def withFilter(newFilter: SearchFilterExpr): QueryBuilder[T, D, N]

  def withFilter(filter: Option[SearchFilterExpr]): QueryBuilder[T, D, N] = {
    filter.fold(this)(withFilter)
  }

  def resetFilter: QueryBuilder[T, D, N] = withFilter(SearchFilterExpr.Empty)


  def withSorting(newSorting: Sorting): QueryBuilder[T, D, N]

  def withSorting(sorting: Option[Sorting]): QueryBuilder[T, D, N] = {
    sorting.fold(this)(withSorting)
  }

  def resetSorting: QueryBuilder[T, D, N] = withSorting(Sorting.Empty)


  def withPagination(newPagination: Pagination): QueryBuilder[T, D, N]

  def withPagination(pagination: Option[Pagination]): QueryBuilder[T, D, N] = {
    pagination.fold(this)(withPagination)
  }

  def resetPagination: QueryBuilder[T, D, N]

}
