package xyz.driver.common.concurrent

import java.util.concurrent.LinkedBlockingQueue

import xyz.driver.common.concurrent.BridgeUploadQueue.Item
import xyz.driver.common.domain.LongId
import xyz.driver.common.logging.PhiLogging

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Use it only for tests
  */
class InMemoryBridgeUploadQueue extends BridgeUploadQueue with PhiLogging {

  private val queue = new LinkedBlockingQueue[Item]()

  override def add(item: Item): Future[Unit] = {
    queue.add(item)
    done
  }

  override def tryRetry(item: Item): Future[Option[Item]] = Future.successful(Some(item))

  override def get(kind: String): Future[Option[Item]] = {
    val r = queue.iterator().asScala.find(_.kind == kind)
    Future.successful(r)
  }

  override def remove(item: LongId[Item]): Future[Unit] = {
    queue.remove(item)
    done
  }

  private val done = Future.successful(())

}
