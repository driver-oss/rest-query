package xyz.driver.pdsuicommon

import java.io.{Closeable, PrintWriter}
import java.net.URL
import java.sql.Connection
import java.util.logging.Logger
import javax.sql.DataSource

import xyz.driver.pdsuicommon.db.SlickQueryBuilder.TableData
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.http.HttpFetcher

import scala.concurrent.Future

class MockDataSource extends DataSource with Closeable {
  override def getConnection: Connection = throw new NotImplementedError("MockDataSource.getConnection")
  override def getConnection(username: String, password: String): Connection = {
    throw new NotImplementedError(s"MockDataSource.getConnection($username, $password)")
  }
  override def close(): Unit                          = throw new NotImplementedError("MockDataSource.close")
  override def setLogWriter(out: PrintWriter): Unit   = throw new NotImplementedError("MockDataSource.setLogWriter")
  override def getLoginTimeout: Int                   = throw new NotImplementedError("MockDataSource.getLoginTimeout")
  override def setLoginTimeout(seconds: Int): Unit    = throw new NotImplementedError("MockDataSource.setLoginTimeout")
  override def getParentLogger: Logger                = throw new NotImplementedError("MockDataSource.getParentLogger")
  override def getLogWriter: PrintWriter              = throw new NotImplementedError("MockDataSource.getLogWriter")
  override def unwrap[T](iface: Class[T]): T          = throw new NotImplementedError("MockDataSource.unwrap")
  override def isWrapperFor(iface: Class[_]): Boolean = throw new NotImplementedError("MockDataSource.isWrapperFor")
}

class MockFactory()(implicit val sqlContext: PostgresContext) {
  val MockHttpFetcher: HttpFetcher = { (url: URL) =>
    Future.successful(Array.empty[Byte])
  }
}

object MockQueryBuilder {

  type MockRunnerIn       = (SearchFilterExpr, Sorting, Option[Pagination])
  type MockRunnerOut[T]   = Future[Seq[T]]
  type MockCountRunnerOut = SlickQueryBuilder.CountResult

  def apply[T](matcher: PartialFunction[MockRunnerIn, MockRunnerOut[T]])(
          countMatcher: PartialFunction[MockRunnerIn, MockCountRunnerOut])(
          implicit context: PostgresContext): SlickQueryBuilder[T] = {

    val runner: SlickQueryBuilder.Runner[T] = { parameters =>
      matcher((parameters.filter, parameters.sorting, parameters.pagination))
    }

    val countRunner: SlickQueryBuilder.CountRunner = { parameters =>
      countMatcher((parameters.filter, parameters.sorting, parameters.pagination))
    }

    val parameters = SlickPostgresQueryBuilderParameters(
      databaseName = "test",
      tableData = TableData("", None, Set.empty[String]),
      links = Map.empty
    )
    new SlickPostgresQueryBuilder(parameters)(runner, countRunner)
  }
}
