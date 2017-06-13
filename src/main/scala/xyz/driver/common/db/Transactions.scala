package xyz.driver.common.db

import xyz.driver.common.logging.PhiLogging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Transactions()(implicit context: SqlContext) extends PhiLogging {
  def run[T](f: SqlContext => T): Future[T] = {
    import context.executionContext

    Future(context.transaction(f(context))).andThen {
      case Failure(e) => logger.error(phi"Can't run a transaction: $e")
    }
  }

  def runSync[T](f: SqlContext => T): Unit = {
    Try(context.transaction(f(context))) match {
      case Success(_) =>
      case Failure(e) => logger.error(phi"Can't run a transaction: $e")
    }
  }
}
