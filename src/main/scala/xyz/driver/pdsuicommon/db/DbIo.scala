package xyz.driver.pdsuicommon.db

import scala.concurrent.Future

/**
  * Where queries should run
  */
trait DbIo {
  def runAsync[T](f: => T): Future[T]
  def runSync[T](f: => T): T = f
  def runAsyncTx[T](f: => T): Future[T]
  def runSyncTx[T](f: => T): Unit
}
