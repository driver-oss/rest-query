package xyz.driver.restquery.db

import java.sql.PreparedStatement

import slick.jdbc.{JdbcProfile, SQLActionBuilder}
import xyz.driver.restquery.query.Sorting.{Dimension, Sequential}
import xyz.driver.restquery.query.SortingOrder.{Ascending, Descending}
import xyz.driver.restquery.query._

object SlickQueryBuilderParameters {
  val AllFields = Set("*")
}

trait SlickQueryBuilderParameters {
  import SlickQueryBuilder._

  def databaseName: String
  def tableData: SlickQueryBuilder.TableData
  def links: Map[String, SlickTableLink]
  def filter: SearchFilterExpr
  def sorting: Sorting
  def pagination: Option[Pagination]

  def qs: String

  def findLink(tableName: String): SlickTableLink = links.get(tableName) match {
    case None       => throw new IllegalArgumentException(s"Cannot find a link for `$tableName`")
    case Some(link) => link
  }

  def toSql(countQuery: Boolean = false)(implicit profile: JdbcProfile): SQLActionBuilder = {
    toSql(countQuery, SlickQueryBuilderParameters.AllFields)
  }

  def toSql(countQuery: Boolean, fields: Set[String])(implicit profile: JdbcProfile): SQLActionBuilder = {
    import profile.api._
    val escapedTableName = s"""$qs$databaseName$qs.$qs${tableData.tableName}$qs"""
    val fieldsSql: String = if (countQuery) {
      val suffix: String = tableData.lastUpdateFieldName match {
        case Some(lastUpdateField) => s", max($escapedTableName.$qs$lastUpdateField$qs)"
        case None                  => ""
      }
      s"count(*) $suffix"
    } else {
      if (fields == SlickQueryBuilderParameters.AllFields) {
        s"$escapedTableName.*"
      } else {
        fields
          .map { field =>
            s"$escapedTableName.$qs$field$qs"
          }
          .mkString(", ")
      }
    }
    val where   = filterToSql(escapedTableName, filter)
    val orderBy = sortingToSql(escapedTableName, sorting)

    val limitSql = limitToSql()

    val sql = sql"""select #$fieldsSql from #$escapedTableName"""

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
            val escapedForeignTableName = s"$qs$databaseName$qs.$qs$foreignTableName$qs"
            val join                    = sql""" inner join #$escapedForeignTableName
             on #$escapedTableName.#$qs#$keyColumnName#$qs=#$escapedForeignTableName.#$qs#$foreignKeyColumnName#$qs"""
            fkSql(fkLinksSql concat join, tableLinks.tail)
        }
      } else fkLinksSql
    }
    val foreignTableLinksSql = fkSql(sql"", foreignTableLinks)

    val whereSql = if (where.queryParts.size > 1) {
      sql" where " concat where
    } else sql""

    val orderSql = if (orderBy.nonEmpty && !countQuery) {
      sql" order by #$orderBy"
    } else sql""

    val limSql = if (limitSql.queryParts.size > 1 && !countQuery) {
      sql" " concat limitSql
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
      s"${dimension.tableName.map(t => s"$qs$databaseName$qs.$qs$t$qs").getOrElse(escapedTableName)}.$qs${dimension.name}$qs"
    }

    def filterToSqlMultiple(operands: Seq[SearchFilterExpr]) = operands.collect {
      case x if !SearchFilterExpr.isEmpty(x) => filterToSql(escapedTableName, x)
    }

    def multipleSqlToAction(
        first: Boolean,
        op: String,
        conditions: Seq[SQLActionBuilder],
        sql: SQLActionBuilder): SQLActionBuilder = {
      if (conditions.nonEmpty) {
        val condition = conditions.head
        if (first) {
          multipleSqlToAction(first = false, op, conditions.tail, condition)
        } else {
          multipleSqlToAction(first = false, op, conditions.tail, sql concat sql" #${op} " concat condition)
        }
      } else sql
    }

    def concatenateParameters(sql: SQLActionBuilder, first: Boolean, tail: Seq[AnyRef]): SQLActionBuilder = {
      if (tail.nonEmpty) {
        if (!first) {
          concatenateParameters(sql concat sql""",${tail.head}""", first = false, tail.tail)
        } else {
          concatenateParameters(sql"""(${tail.head}""", first = false, tail.tail)
        }
      } else sql concat sql")"
    }

    filter match {
      case x if isEmpty(x) =>
        sql""

      case AllowAll =>
        sql"1=1"

      case DenyAll =>
        sql"1=0"

      case Atom.Binary(dimension, Eq, value) if isNull(value) =>
        sql"#${escapeDimension(dimension)} is NULL"

      case Atom.Binary(dimension, NotEq, value) if isNull(value) =>
        sql"#${escapeDimension(dimension)} is not NULL"

      case Atom.Binary(dimension, NotEq, value) if tableData.nullableFields.contains(dimension.name) =>
        // In MySQL NULL <> Any === NULL
        // So, to handle NotEq for nullable fields we need to use more complex SQL expression.
        // http://dev.mysql.com/doc/refman/5.7/en/working-with-null.html
        val escapedColumn = escapeDimension(dimension)
        sql"(#${escapedColumn} is null or #${escapedColumn} != $value)"

      case Atom.Binary(dimension, op, value) =>
        val operator = op match {
          case Eq    => sql"="
          case NotEq => sql"!="
          case Like  => sql" like "
          case Gt    => sql">"
          case GtEq  => sql">="
          case Lt    => sql"<"
          case LtEq  => sql"<="
        }
        sql"#${escapeDimension(dimension)}" concat operator concat sql"""$value"""

      case Atom.NAry(dimension, op, values) =>
        val sqlOp = op match {
          case SearchFilterNAryOperation.In    => sql" in "
          case SearchFilterNAryOperation.NotIn => sql" not in "
        }

        if (values.nonEmpty) {
          val formattedValues = concatenateParameters(sql"", first = true, values)
          sql"#${escapeDimension(dimension)}" concat sqlOp concat formattedValues
        } else {
          sql"1=0"
        }

      case Intersection(operands) =>
        val filter = multipleSqlToAction(first = true, "and", filterToSqlMultiple(operands), sql"")
        sql"(" concat filter concat sql")"

      case Union(operands) =>
        val filter = multipleSqlToAction(first = true, "or", filterToSqlMultiple(operands), sql"")
        sql"(" concat filter concat sql")"
    }
  }

  protected def limitToSql()(implicit profile: JdbcProfile): SQLActionBuilder

  /**
    * @param escapedMainTableName Should be escaped
    */
  protected def sortingToSql(escapedMainTableName: String, sorting: Sorting)(implicit profile: JdbcProfile): String = {
    sorting match {
      case Dimension(optSortingTableName, field, order) =>
        val sortingTableName =
          optSortingTableName.map(x => s"$qs$databaseName$qs.$qs$x$qs").getOrElse(escapedMainTableName)
        val fullName = s"$sortingTableName.$qs$field$qs"

        s"$fullName ${orderToSql(order)}"

      case Sequential(xs) =>
        xs.map(sortingToSql(escapedMainTableName, _)).mkString(", ")
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
