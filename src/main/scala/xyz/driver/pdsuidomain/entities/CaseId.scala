package xyz.driver.pdsuidomain.entities

import java.util.UUID

final case class CaseId(id: String)

object CaseId {

  def apply() = new CaseId(UUID.randomUUID().toString)
}
