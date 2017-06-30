package xyz.driver.pdsuicommon.db.repositories

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.db.MysqlQueryBuilder

trait BridgeUploadQueueRepository extends Repository {

  type EntityT = BridgeUploadQueue.Item

  def add(draft: EntityT): EntityT

  def getById(kind: String, tag: String): Option[EntityT]

  def getOne(kind: String): Option[BridgeUploadQueue.Item]

  def update(entity: EntityT): EntityT

  def delete(kind: String, tag: String): Unit

  def buildQuery: MysqlQueryBuilder[EntityT]
}
