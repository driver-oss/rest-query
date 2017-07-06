package xyz.driver.pdsuicommon.concurrent

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Dependency
import xyz.driver.pdsuicommon.concurrent.SafeBridgeUploadQueue.{DependencyResolver, SafeTask, Tag}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.serialization.Marshaller

import scala.concurrent.{ExecutionContext, Future}

object SafeBridgeUploadQueue {

  trait Tag extends Product with Serializable

  final case class SafeTask[T <: Tag](tag: T, private[SafeBridgeUploadQueue] val queueItem: BridgeUploadQueue.Item)

  object SafeTask {
    implicit def toPhiString[T <: Tag](x: SafeTask[T]): PhiString = {
      import x._
      phi"SafeTask(tag=${Unsafe(tag)}, $queueItem)"
    }
  }

  trait DependencyResolver[T <: Tag] {
    def getDependency(tag: T): Option[Dependency]
  }

}

class SafeBridgeUploadQueue[T <: Tag](kind: String, origQueue: BridgeUploadQueue)(
        implicit tagMarshaller: Marshaller[T, String],
        dependencyResolver: DependencyResolver[T],
        executionContext: ExecutionContext) {

  type Task = SafeTask[T]

  def add(tag: T): Future[BridgeUploadQueue.Item] =
    origQueue.add(
      BridgeUploadQueue.Item(
        kind = kind,
        tag = tagMarshaller.write(tag),
        dependency = dependencyResolver.getDependency(tag)
      ))

  def tryRetry(task: Task): Future[Option[Task]] = wrap(origQueue.tryRetry(task.queueItem))

  def get: Future[Option[Task]] = wrap(origQueue.get(kind))

  def complete(tag: T): Future[Unit] = origQueue.complete(kind, tagMarshaller.write(tag))

  private def wrap(x: Future[Option[BridgeUploadQueue.Item]]): Future[Option[Task]] = x.map(_.map(cover))

  private def cover(rawTask: BridgeUploadQueue.Item): Task = {
    val tag = tagMarshaller
      .read(rawTask.tag)
      .getOrElse(throw new IllegalArgumentException(s"Can not parse tag '${rawTask.tag}'"))

    SafeTask(tag, rawTask)
  }

}
