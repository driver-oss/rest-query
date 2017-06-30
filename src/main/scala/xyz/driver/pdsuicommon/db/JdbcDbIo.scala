package xyz.driver.pdsuicommon.db

import xyz.driver.pdsuicommon.logging._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class JdbcDbIo(sqlContext: TransactionalContext) extends DbIo with PhiLogging {

  override def runAsync[T](f: => T): Future[T] = {
    Future(f)(sqlContext.executionContext)
  }

  override def runAsyncTx[T](f: => T): Future[T] = {
    import sqlContext.executionContext

    Future(sqlContext.transaction(f)).andThen {
      case Failure(e) => logger.error(phi"Can't run a transaction: $e")
    }
  }

  override def runSyncTx[T](f: => T): Unit = {
    Try(sqlContext.transaction(f)) match {
      case Success(_) =>
      case Failure(e) => logger.error(phi"Can't run a transaction: $e")
    }
  }
}
