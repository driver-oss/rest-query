package xyz.driver.pdsuidomain.entities

import java.util.UUID

final case class PatientOrderId(id: UUID) {
  override def toString: String = id.toString
}

object PatientOrderId {

  def apply() = new PatientOrderId(UUID.randomUUID())

  def apply(x: String) = new PatientOrderId(UUID.fromString(x))
}
