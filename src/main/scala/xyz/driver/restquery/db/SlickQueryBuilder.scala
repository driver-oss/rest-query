package xyz.driver.restquery.db

import java.sql.{JDBCType, PreparedStatement}
import java.time.LocalDateTime

import slick.jdbc._
import xyz.driver.restquery.query._

import scala.concurrent.{ExecutionContext, Future}

object SlickQueryBuilder {

  type Runner[T] = SlickQueryBuilderParameters => Future[Seq[T]]

  type CountResult = Future[(Int, Option[LocalDateTime])]

  type CountRunner = SlickQueryBuilderParameters => CountResult

  /**
    * Binder for PreparedStatement
    */
  type Binder = PreparedStatement => PreparedStatement

  final case class TableData(
      tableName: String,
      lastUpdateFieldName: Option[String] = None,
      nullableFields: Set[String] = Set.empty)

  val AllFields = Set("*")

  implicit class SQLActionBuilderConcat(a: SQLActionBuilder) {
    def concat(b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(
        a.queryParts ++ b.queryParts,
        (p: Unit, pp: PositionedParameters) => {
          a.unitPConv.apply(p, pp)
          b.unitPConv.apply(p, pp)
        }
      )
    }
  }

  implicit object SetQueryParameter extends SetParameter[AnyRef] {
    def apply(v: AnyRef, pp: PositionedParameters) = {
      pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber)
    }
  }
}

final case class SlickTableLink(keyColumnName: String, foreignTableName: String, foreignKeyColumnName: String)

final case class SlickPostgresQueryBuilderParameters(
    databaseName: String,
    tableData: SlickQueryBuilder.TableData,
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
        sql"limit #${pagination.pageSize} OFFSET #$startFrom"
      }
      .getOrElse(sql"")
  }

  val qs = """""""

}

/**
  * @param links Links to another tables grouped by foreignTableName
  */
final case class SlickMysqlQueryBuilderParameters(
    databaseName: String,
    tableData: SlickQueryBuilder.TableData,
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
        sql"limit #$startFrom, #${pagination.pageSize}"
      }
      .getOrElse(sql"")
  }

  val qs = """`"""

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
