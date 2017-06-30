package xyz.driver.pdsuidomain.storage

import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Arm, Patient}

import scala.collection.concurrent.TrieMap

object RequestStorage {
  type Key   = (UuidId[Patient], String)
  type Value = Set[LongId[Arm]]
}

class RequestStorage extends PhiLogging {
  import RequestStorage._

  private val storage = new TrieMap[Key, Value]()

  def put(patientId: UuidId[Patient], disease: String, ineligibleArms: Set[LongId[Arm]]): Unit = {
    logger.debug(phi"put($patientId, ${Unsafe(disease)}, $ineligibleArms")
    val key = (patientId, disease.toLowerCase)
    get(patientId, disease.toLowerCase) match {
      case Some(oldValue) =>
        logger.trace(phi"Requested ineligible arms=$oldValue, replace it")
        storage.replace(key, oldValue, ineligibleArms)
      case None =>
        logger.trace(phi"Put request into storage")
        storage.put(key, ineligibleArms)
    }
  }

  def get(patientId: UuidId[Patient], disease: String): Option[Value] = {
    logger.debug(phi"get($patientId, ${Unsafe(disease)}")
    val key = (patientId, disease.toLowerCase)
    storage.get(key)
  }

  def contains(patientId: UuidId[Patient], disease: String, value: Set[LongId[Arm]]): Boolean = {
    logger.debug(phi"contains(key=($patientId,${Unsafe(disease)}), value=$value")
    get(patientId, disease.toLowerCase).contains(value)
  }

  def remove(patientId: UuidId[Patient], disease: String): Unit = {
    logger.debug(phi"remove($patientId,${Unsafe(disease)}")
    val key = (patientId, disease.toLowerCase)
    storage.remove(key)
  }
}
