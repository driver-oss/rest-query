package xyz.driver.pdsuidomain.entities

import java.util.UUID
import xyz.driver.pdsuicommon.logging._

final case class RecordRequestId(id: UUID) {
  override def toString: String = id.toString
}

object RecordRequestId {

  def apply() = new RecordRequestId(UUID.randomUUID())

  implicit def toPhiString(x: RecordRequestId): PhiString = phi"${x.id}"
}
