package xyz.driver.common.domain

import java.util.UUID

import xyz.driver.common.logging._

case class RecordRequestId(id: UUID) {
  override def toString: String = id.toString
}

object RecordRequestId {

  def apply() = new RecordRequestId(UUID.randomUUID())

  implicit def toPhiString(x: RecordRequestId): PhiString = phi"${x.id}"
}
