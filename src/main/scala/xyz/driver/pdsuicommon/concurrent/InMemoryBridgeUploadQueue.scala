package xyz.driver.pdsuicommon.concurrent

import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Predicate

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item
import xyz.driver.pdsuicommon.logging.PhiLogging

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Use it only for tests
  */
class InMemoryBridgeUploadQueue extends BridgeUploadQueue with PhiLogging {

  private val queue = new LinkedBlockingQueue[Item]()

  override def add(item: Item): Future[Item] = {
    queue.add(item)
    Future.successful(item)
  }

  override def tryRetry(item: Item): Future[Option[Item]] = Future.successful(Some(item))

  override def get(kind: String): Future[Option[Item]] = {
    val r = queue.iterator().asScala.find(_.kind == kind)
    Future.successful(r)
  }

  override def complete(kind: String, tag: String): Future[Unit] = {
    queue.removeIf(new Predicate[Item] {
      override def test(t: Item): Boolean = t.kind == kind && t.tag == tag
    })
    Future.successful(())
  }
}
