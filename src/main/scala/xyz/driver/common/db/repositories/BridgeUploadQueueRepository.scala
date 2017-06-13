package xyz.driver.common.db.repositories

import xyz.driver.common.concurrent.BridgeUploadQueue
import xyz.driver.common.domain.LongId

import scala.concurrent.Future

trait BridgeUploadQueueRepository extends Repository {

  type EntityT = BridgeUploadQueue.Item
  type IdT = LongId[EntityT]

  def add(draft: EntityT): EntityT

  def getById(id: LongId[EntityT]): Option[EntityT]

  def isCompleted(kind: String, tag: String): Future[Boolean]

  def getOne(kind: String): Future[Option[BridgeUploadQueue.Item]]

  def update(entity: EntityT): EntityT

  def delete(id: IdT): Unit
}
