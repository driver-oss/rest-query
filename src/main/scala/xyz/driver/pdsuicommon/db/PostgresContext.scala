package xyz.driver.pdsuicommon.db

import java.io.Closeable
import java.util.concurrent.Executors
import javax.sql.DataSource

import io.getquill._
import xyz.driver.pdsuicommon.concurrent.MdcExecutionContext
import xyz.driver.pdsuicommon.db.PostgresContext.Settings
import xyz.driver.pdsuicommon.logging._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object PostgresContext extends PhiLogging {

  final case class Settings(connection: com.typesafe.config.Config,
                            connectionAttemptsOnStartup: Int,
                            threadPoolSize: Int)

  def apply(settings: Settings): PostgresContext = {
    // Prevent leaking credentials to a log
    Try(JdbcContextConfig(settings.connection).dataSource) match {
      case Success(dataSource) => new PostgresContext(dataSource, settings)
      case Failure(NonFatal(e)) =>
        logger.error(phi"Can not load dataSource, error: ${Unsafe(e.getClass.getName)}")
        throw new IllegalArgumentException("Can not load dataSource from config. Check your database and config", e)
    }
  }

}

class PostgresContext(val dataSource: DataSource with Closeable, settings: Settings)
    extends PostgresJdbcContext[SnakeCase](dataSource) with TransactionalContext {

  private val tpe = Executors.newFixedThreadPool(settings.threadPoolSize)

  implicit val executionContext: ExecutionContext = {
    val orig = ExecutionContext.fromExecutor(tpe)
    MdcExecutionContext.from(orig)
  }

  override def close(): Unit = {
    super.close()
    tpe.shutdownNow()
  }
}
