package xyz.driver.pdsuicommon.concurrent

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

import scala.concurrent.Future

object BridgeUploadQueue {

  /**
    * @param kind        For example documents
    * @param tag         For example, a patient's id: 1
    * @param attempts    Which attempt
    * @param created     When the task was created
    * @param nextAttempt Time of the next attempt
    */
  final case class Item(id: LongId[Item],
                        kind: String,
                        tag: String,
                        created: LocalDateTime,
                        attempts: Int,
                        nextAttempt: LocalDateTime,
                        completed: Boolean,
                        dependencyKind: Option[String],
                        dependencyTag: Option[String]) {

    def dependency: Option[Dependency] = {
      dependencyKind
        .zip(dependencyTag)
        .headOption
        .map(Function.tupled(Dependency.apply))
    }

  }

  object Item {

    implicit def toPhiString(x: Item): PhiString = {
      import x._
      phi"BridgeUploadQueue.Item(id=$id, kind=${Unsafe(kind)}, tag=${Unsafe(tag)}, " +
        phi"attempts=${Unsafe(attempts)}, start=$created, nextAttempt=$nextAttempt, completed=$completed, " +
        phi"dependency=$dependency)"
    }

    def apply(kind: String, tag: String, dependency: Option[Dependency] = None): Item = {
      val now = LocalDateTime.now()

      Item(
        id = LongId(0),
        kind = kind,
        tag = tag,
        created = now,
        attempts = 0,
        nextAttempt = now,
        completed = false,
        dependencyKind = dependency.map(_.kind),
        dependencyTag = dependency.map(_.tag)
      )
    }

  }

  final case class Dependency(kind: String, tag: String)

  object Dependency {

    implicit def toPhiString(x: Dependency): PhiString = {
      import x._
      phi"Dependency(kind=${Unsafe(kind)}, tag=${Unsafe(tag)})"
    }
  }
}

trait BridgeUploadQueue {

  def add(item: Item): Future[Unit]

  def get(kind: String): Future[Option[Item]]

  def remove(item: LongId[Item]): Future[Unit]

  def tryRetry(item: Item): Future[Option[Item]]

}
