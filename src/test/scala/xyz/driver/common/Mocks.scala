package xyz.driver.common

import java.io.{Closeable, PrintWriter}
import java.net.URL
import java.sql.Connection
import java.util.logging.Logger
import javax.sql.DataSource

import com.typesafe.config.ConfigFactory
import xyz.driver.common.db._
import xyz.driver.common.http.HttpFetcher

import scala.concurrent.{ExecutionContext, Future}

class MockDataSource extends DataSource with Closeable {
  override def getConnection: Connection = throw new NotImplementedError("MockDataSource.getConnection")
  override def getConnection(username: String, password: String): Connection = {
    throw new NotImplementedError(s"MockDataSource.getConnection($username, $password)")
  }
  override def close(): Unit = throw new NotImplementedError("MockDataSource.close")
  override def setLogWriter(out: PrintWriter): Unit = throw new NotImplementedError("MockDataSource.setLogWriter")
  override def getLoginTimeout: Int = throw new NotImplementedError("MockDataSource.getLoginTimeout")
  override def setLoginTimeout(seconds: Int): Unit = throw new NotImplementedError("MockDataSource.setLoginTimeout")
  override def getParentLogger: Logger = throw new NotImplementedError("MockDataSource.getParentLogger")
  override def getLogWriter: PrintWriter = throw new NotImplementedError("MockDataSource.getLogWriter")
  override def unwrap[T](iface: Class[T]): T = throw new NotImplementedError("MockDataSource.unwrap")
  override def isWrapperFor(iface: Class[_]): Boolean = throw new NotImplementedError("MockDataSource.isWrapperFor")
}

object MockSqlContext {

  val Settings = SqlContext.Settings(
    credentials = SqlContext.DbCredentials(
      user = "test",
      password = "test",
      host = "localhost",
      port = 3248,
      dbName = "test",
      dbCreateFlag = false,
      dbContext = "test",
      connectionParams = "",
      url = ""
    ),
    connection = ConfigFactory.empty(),
    connectionAttemptsOnStartup = 1,
    threadPoolSize = 10
  )

}

class MockSqlContext(ec: ExecutionContext) extends SqlContext(new MockDataSource, MockSqlContext.Settings) {
  override implicit val executionContext = ec
  override protected def withConnection[T](f: Connection => T) = {
    throw new NotImplementedError("MockSqlContext.withConnection")
  }
}

class MockFactory()(implicit val sqlContext: SqlContext) {
  val MockHttpFetcher: HttpFetcher = (url: URL) => {
    Future.successful(Array.empty[Byte])
  }
}

object MockQueryBuilder {

  type MockRunnerIn = (SearchFilterExpr, Sorting, Option[Pagination])
  type MockRunnerOut[T] = Future[Seq[T]]
  type MockCountRunnerOut = Future[QueryBuilder.CountResult]

  def apply[T](matcher: PartialFunction[MockRunnerIn, MockRunnerOut[T]])
              (countMatcher: PartialFunction[MockRunnerIn, MockCountRunnerOut])
              (implicit context: SqlContext): MysqlQueryBuilder[T] = {
    def runner(parameters: QueryBuilderParameters): MockRunnerOut[T] = {
      matcher((parameters.filter, parameters.sorting, parameters.pagination))
    }
    def countRunner(parameters: QueryBuilderParameters): MockCountRunnerOut = {
      countMatcher((parameters.filter, parameters.sorting, parameters.pagination))
    }
    MysqlQueryBuilder[T](
      tableName = "",
      lastUpdateFieldName = Option.empty[String],
      nullableFields = Set.empty[String],
      links = Set.empty[TableLink],
      runner = runner _,
      countRunner = countRunner _
    )(context.executionContext)
  }
}

