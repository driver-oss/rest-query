package xyz.driver.pdsuicommon.db

import scala.concurrent.Future

object FakeDbIo extends DbIo {
  override def runAsync[T](f: => T): Future[T]   = Future.successful(f)
  override def runAsyncTx[T](f: => T): Future[T] = Future.successful(f)
  override def runSyncTx[T](f: => T): Unit       = f
}
