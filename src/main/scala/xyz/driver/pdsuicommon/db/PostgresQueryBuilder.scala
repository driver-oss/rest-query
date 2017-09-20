package xyz.driver.pdsuicommon.db

import java.sql.ResultSet

import io.getquill.{PostgresDialect, PostgresEscape}
import xyz.driver.pdsuicommon.db.PostgresQueryBuilder.SmartPostgresEscape

import scala.collection.breakOut

object PostgresQueryBuilder {

  import xyz.driver.pdsuicommon.db.QueryBuilder._

  trait SmartPostgresEscape extends PostgresEscape {
    override def column(s: String): String =
      if (s.startsWith("$")) s else super.column(s)
    override def default(s: String): String =
      s.split("\\.").map(ss => s""""$ss"""").mkString(".")
  }

  object SmartPostgresEscape extends SmartPostgresEscape

  type Escape = SmartPostgresEscape
  val Escape = SmartPostgresEscape

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               runner: Runner[T],
               countRunner: CountRunner): PostgresQueryBuilder[T] = {
    val parameters = PostgresQueryBuilderParameters(
      tableData = TableData(tableName, lastUpdateFieldName, nullableFields),
      links = links.map(x => x.foreignTableName -> x)(breakOut)
    )
    new PostgresQueryBuilder[T](parameters)(runner, countRunner)
  }

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               extractor: ResultSet => T)(implicit sqlContext: PostgresContext): PostgresQueryBuilder[T] = {
    apply(tableName, QueryBuilderParameters.AllFields, lastUpdateFieldName, nullableFields, links, extractor)
  }

  def apply[T](tableName: String,
               fields: Set[String],
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               extractor: ResultSet => T)(implicit sqlContext: PostgresContext): PostgresQueryBuilder[T] = {

    val runner: Runner[T] = { parameters =>
      val (sql, binder) = parameters.toSql(countQuery = false, fields = fields, namingStrategy = SmartPostgresEscape)
      sqlContext.executeQuery[T](sql, binder, { resultSet =>
        extractor(resultSet)
      })
    }

    val countRunner: CountRunner = { parameters =>
      val (sql, binder) = parameters.toSql(countQuery = true, namingStrategy = SmartPostgresEscape)
      sqlContext
        .executeQuery[CountResult](
          sql,
          binder, { resultSet =>
            val count = resultSet.getInt(1)
            val lastUpdate = if (parameters.tableData.lastUpdateFieldName.isDefined) {
              Option(resultSet.getTimestamp(2)).map(sqlContext.timestampToLocalDateTime)
            } else None

            (count, lastUpdate)
          }
        )
        .head
    }

    apply[T](
      tableName = tableName,
      lastUpdateFieldName = lastUpdateFieldName,
      nullableFields = nullableFields,
      links = links,
      runner = runner,
      countRunner = countRunner
    )
  }
}

class PostgresQueryBuilder[T](parameters: PostgresQueryBuilderParameters)(implicit runner: QueryBuilder.Runner[T],
                                                                          countRunner: QueryBuilder.CountRunner)
    extends QueryBuilder[T, PostgresDialect, PostgresQueryBuilder.Escape](parameters) {

  def withFilter(newFilter: SearchFilterExpr): QueryBuilder[T, PostgresDialect, SmartPostgresEscape] = {
    new PostgresQueryBuilder[T](parameters.copy(filter = newFilter))
  }

  def withSorting(newSorting: Sorting): QueryBuilder[T, PostgresDialect, SmartPostgresEscape] = {
    new PostgresQueryBuilder[T](parameters.copy(sorting = newSorting))
  }

  def withPagination(newPagination: Pagination): QueryBuilder[T, PostgresDialect, SmartPostgresEscape] = {
    new PostgresQueryBuilder[T](parameters.copy(pagination = Some(newPagination)))
  }

  def resetPagination: QueryBuilder[T, PostgresDialect, SmartPostgresEscape] = {
    new PostgresQueryBuilder[T](parameters.copy(pagination = None))
  }
}
