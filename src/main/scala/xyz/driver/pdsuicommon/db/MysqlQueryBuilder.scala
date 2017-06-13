package xyz.driver.pdsuicommon.db

import java.sql.ResultSet

import io.getquill.{MySQLDialect, MysqlEscape}

import scala.collection.breakOut
import scala.concurrent.{ExecutionContext, Future}

object MysqlQueryBuilder {
  import xyz.driver.pdsuicommon.db.QueryBuilder._

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               runner: Runner[T],
               countRunner: CountRunner)
              (implicit ec: ExecutionContext): MysqlQueryBuilder[T] = {
    val parameters = MysqlQueryBuilderParameters(
      tableData = TableData(tableName, lastUpdateFieldName, nullableFields),
      links = links.map(x => x.foreignTableName -> x)(breakOut)
    )
    new MysqlQueryBuilder[T](parameters)(runner, countRunner, ec)
  }

  def apply[T](tableName: String,
               lastUpdateFieldName: Option[String],
               nullableFields: Set[String],
               links: Set[TableLink],
               extractor: (ResultSet) => T)
              (implicit sqlContext: SqlContext): MysqlQueryBuilder[T] = {

    val runner = (parameters: QueryBuilderParameters) => {
      Future {
        val (sql, binder) = parameters.toSql(namingStrategy = MysqlEscape)
        sqlContext.executeQuery[T](sql, binder, { resultSet =>
          extractor(resultSet)
        })
      }(sqlContext.executionContext)
    }

    val countRunner = (parameters: QueryBuilderParameters) => {
      Future {
        val (sql, binder) = parameters.toSql(countQuery = true, namingStrategy = MysqlEscape)
        sqlContext.executeQuery[CountResult](sql, binder, { resultSet =>
          val count = resultSet.getInt(1)
          val lastUpdate = if (parameters.tableData.lastUpdateFieldName.isDefined) {
            Option(sqlContext.localDateTimeDecoder.decoder(2, resultSet))
          } else None

          (count, lastUpdate)
        }).head
      }(sqlContext.executionContext)
    }

    apply[T](
      tableName = tableName,
      lastUpdateFieldName = lastUpdateFieldName,
      nullableFields = nullableFields,
      links = links,
      runner = runner,
      countRunner = countRunner
    )(sqlContext.executionContext)
  }
}

class MysqlQueryBuilder[T](parameters: MysqlQueryBuilderParameters)
                          (implicit runner: QueryBuilder.Runner[T],
                           countRunner: QueryBuilder.CountRunner,
                           ec: ExecutionContext)
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
