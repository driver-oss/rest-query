package xyz.driver.pdsuicommon.db

import scala.concurrent.ExecutionContext

trait TransactionalContext {

  implicit def executionContext: ExecutionContext

  def transaction[T](f: => T): T

}
