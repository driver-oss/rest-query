package xyz.driver.restquery.db

import java.time.{LocalDateTime, ZoneOffset}

import org.slf4j.LoggerFactory
import slick.jdbc.{GetResult, JdbcProfile}
import xyz.driver.core.database.SlickDal
import xyz.driver.restquery.query.{Pagination, SearchFilterExpr, Sorting}

import scala.collection.breakOut
import scala.concurrent.ExecutionContext

object SlickPostgresQueryBuilder {
  private val logger = LoggerFactory.getLogger(this.getClass)

  import xyz.driver.restquery.db.SlickQueryBuilder._

  def apply[T](
      databaseName: String,
      tableName: String,
      lastUpdateFieldName: Option[String],
      nullableFields: Set[String],
      links: Set[SlickTableLink],
      runner: Runner[T],
      countRunner: CountRunner)(
      implicit sqlContext: SlickDal,
      profile: JdbcProfile,
      getResult: GetResult[T],
      ec: ExecutionContext): SlickPostgresQueryBuilder[T] = {
    val parameters = SlickPostgresQueryBuilderParameters(
      databaseName = databaseName,
      tableData = TableData(tableName, lastUpdateFieldName, nullableFields),
      links = links.map(x => x.foreignTableName -> x)(breakOut)
    )
    new SlickPostgresQueryBuilder[T](parameters)(runner, countRunner)
  }

  def apply[T](
      databaseName: String,
      tableName: String,
      lastUpdateFieldName: Option[String],
      nullableFields: Set[String],
      links: Set[SlickTableLink])(
      implicit sqlContext: SlickDal,
      profile: JdbcProfile,
      getResult: GetResult[T],
      ec: ExecutionContext): SlickPostgresQueryBuilder[T] = {
    apply[T](databaseName, tableName, SlickQueryBuilderParameters.AllFields, lastUpdateFieldName, nullableFields, links)
  }

  def apply[T](
      databaseName: String,
      tableName: String,
      fields: Set[String],
      lastUpdateFieldName: Option[String],
      nullableFields: Set[String],
      links: Set[SlickTableLink])(
      implicit sqlContext: SlickDal,
      profile: JdbcProfile,
      getResult: GetResult[T],
      ec: ExecutionContext): SlickPostgresQueryBuilder[T] = {

    val runner: Runner[T] = { parameters =>
      val sql = parameters.toSql(countQuery = false, fields = fields).as[T]
      logger.debug(s"Built an SQL query: $sql")
      sqlContext.execute(sql)
    }

    val countRunner: CountRunner = { parameters =>
      implicit val getCountResult: GetResult[(Int, Option[LocalDateTime])] = GetResult({ r =>
        val count = r.rs.getInt(1)
        val lastUpdate = if (parameters.tableData.lastUpdateFieldName.isDefined) {
          Option(r.rs.getTimestamp(2)).map(timestampToLocalDateTime)
        } else None
        (count, lastUpdate)
      })
      val sql = parameters.toSql(countQuery = true).as[(Int, Option[LocalDateTime])]
      logger.debug(s"Built an SQL query returning count: $sql")
      sqlContext.execute(sql).map(_.head)
    }

    apply[T](
      databaseName = databaseName,
      tableName = tableName,
      lastUpdateFieldName = lastUpdateFieldName,
      nullableFields = nullableFields,
      links = links,
      runner = runner,
      countRunner = countRunner
    )
  }

  def timestampToLocalDateTime(timestamp: java.sql.Timestamp): LocalDateTime = {
    LocalDateTime.ofInstant(timestamp.toInstant, ZoneOffset.UTC)
  }
}

class SlickPostgresQueryBuilder[T](parameters: SlickPostgresQueryBuilderParameters)(
    implicit runner: SlickQueryBuilder.Runner[T],
    countRunner: SlickQueryBuilder.CountRunner)
    extends SlickQueryBuilder[T](parameters) {

  def withFilter(newFilter: SearchFilterExpr): SlickQueryBuilder[T] = {
    new SlickPostgresQueryBuilder[T](parameters.copy(filter = newFilter))
  }

  def withSorting(newSorting: Sorting): SlickQueryBuilder[T] = {
    new SlickPostgresQueryBuilder[T](parameters.copy(sorting = newSorting))
  }

  def withPagination(newPagination: Pagination): SlickQueryBuilder[T] = {
    new SlickPostgresQueryBuilder[T](parameters.copy(pagination = Some(newPagination)))
  }

  def resetPagination: SlickQueryBuilder[T] = {
    new SlickPostgresQueryBuilder[T](parameters.copy(pagination = None))
  }
}
