package xyz.driver.pdsuicommon.synchronization.db

import slick.dbio.DBIO
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.synchronization.utils.{FakeIdGen, FakeIdMap}

import scala.concurrent.ExecutionContext
import scalaz.Monad

trait SlickDbAction[+T] {
  def entity: T
}

object SlickDbAction {

  final case class Create[T](entity: T) extends SlickDbAction[T]
  final case class Update[T](entity: T) extends SlickDbAction[T]
  final case class Delete[T](entity: T) extends SlickDbAction[T]

  // Use it only inside of a transaction!
  def unsafeRun[T](actions: List[SlickDbAction[T]], dataSource: SlickDataSource[T])(
          implicit core: FakeIdGen[T],
          executionContext: ExecutionContext,
          dbioMonad: Monad[DBIO]): DBIO[FakeIdMap[T]] = {
    unsafeRun(DBIO.successful(FakeIdMap.empty))(actions, dataSource)
  }

  // Use it only inside of a transaction!
  def unsafeRun[T](initial: DBIO[FakeIdMap[T]])(actions: List[SlickDbAction[T]], dataSource: SlickDataSource[T])(
          implicit core: FakeIdGen[T],
          executionContext: ExecutionContext,
          dbioMonad: Monad[DBIO]): DBIO[FakeIdMap[T]] = {
    // TODO Squash Updates and Delete to one operation, when bugs in repositories will be fixed
    actions.foldLeft(initial) {
      case (previousActions, Create(x)) =>
        for {
          r      <- previousActions
          newArm <- dataSource.create(x)
        } yield {
          r + (core(newArm) -> newArm)
        }

      case (previousActions, Update(x)) =>
        for {
          r          <- previousActions
          updatedArm <- dataSource.update(x).getOrElse(x)
        } yield {
          r - core(updatedArm) + (core(updatedArm) -> updatedArm)
        }

      case (previousActions, Delete(_)) if dataSource.isDictionary =>
        previousActions // We don't delete entities from dictionaries

      case (previousActions, Delete(x)) =>
        for {
          r <- previousActions
          _ <- dataSource.delete(x).run
        } yield {
          r - core(x)
        }
    }
  }

  implicit def toPhiString[T](input: SlickDbAction[T])(implicit inner: T => PhiString): PhiString = input match {
    case Create(x) => phi"Create($x)"
    case Update(x) => phi"Update($x)"
    case Delete(x) => phi"Delete($x)"
  }

}
