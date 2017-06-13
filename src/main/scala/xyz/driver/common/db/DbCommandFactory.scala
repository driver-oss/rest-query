package xyz.driver.common.db

import scala.concurrent.{ExecutionContext, Future}

trait DbCommandFactory[T] {
  def createCommand(orig: T)(implicit ec: ExecutionContext): Future[DbCommand]
}

object DbCommandFactory {
  def empty[T]: DbCommandFactory[T] = new DbCommandFactory[T] {
    override def createCommand(orig: T)(implicit ec: ExecutionContext): Future[DbCommand] = Future.successful(DbCommand.Empty)
  }
}

