package xyz.driver.pdsuicommon.db

import java.sql.ResultSet

import xyz.driver.pdsuicommon.logging._
import io.getquill.{MySQLDialect, MysqlEscape}

import scala.collection.breakOut

object MysqlQueryBuilder extends PhiLogging {
  import xyz.driver.pdsuicommon.db.QueryBuilder._

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               runner: Runner[T],
               countRunner: CountRunner): MysqlQueryBuilder[T] = {
    val parameters = MysqlQueryBuilderParameters(
      tableData = TableData(tableName, lastUpdateFieldName, nullableFields),
      links = links.map(x => x.foreignTableName -> x)(breakOut)
    )
    new MysqlQueryBuilder[T](parameters)(runner, countRunner)
  }

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               extractor: (ResultSet) => T)(implicit sqlContext: MySqlContext): MysqlQueryBuilder[T] = {

    val runner: Runner[T] = { parameters =>
      val (sql, binder) = parameters.toSql(namingStrategy = MysqlEscape)
      logger.trace(phi"Query for execute: ${Unsafe(sql)}")
      sqlContext.executeQuery[T](sql, binder, { resultSet =>
        extractor(resultSet)
      })
    }

    val countRunner: CountRunner = { parameters =>
      val (sql, binder) = parameters.toSql(countQuery = true, namingStrategy = MysqlEscape)
      logger.trace(phi"Query for execute: ${Unsafe(sql)}")
      sqlContext
        .executeQuery[CountResult](
          sql,
          binder, { resultSet =>
            val count = resultSet.getInt(1)
            val lastUpdate = if (parameters.tableData.lastUpdateFieldName.isDefined) {
              Option(sqlContext.localDateTimeDecoder.decoder(2, resultSet))
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

class MysqlQueryBuilder[T](parameters: MysqlQueryBuilderParameters)(implicit runner: QueryBuilder.Runner[T],
                                                                    countRunner: QueryBuilder.CountRunner)
    extends QueryBuilder[T, MySQLDialect, MysqlEscape](parameters) {

  def withFilter(newFilter: SearchFilterExpr): QueryBuilder[T, MySQLDialect, MysqlEscape] = {
    new MysqlQueryBuilder[T](parameters.copy(filter = newFilter))
  }

  def withSorting(newSorting: Sorting): QueryBuilder[T, MySQLDialect, MysqlEscape] = {
    new MysqlQueryBuilder[T](parameters.copy(sorting = newSorting))
  }

  def withPagination(newPagination: Pagination): QueryBuilder[T, MySQLDialect, MysqlEscape] = {
    new MysqlQueryBuilder[T](parameters.copy(pagination = Some(newPagination)))
  }

  def resetPagination: QueryBuilder[T, MySQLDialect, MysqlEscape] = {
    new MysqlQueryBuilder[T](parameters.copy(pagination = None))
  }
}
