package xyz.driver.pdsuicommon.db

import scala.concurrent.Future

trait DbCommand {
  def runSync(): Unit
  def runAsync(transactions: DbIo): Future[Unit]
}

object DbCommand {
  val Empty: DbCommand = new DbCommand {
    override def runSync(): Unit                            = {}
    override def runAsync(transactions: DbIo): Future[Unit] = Future.successful(())
  }
}
