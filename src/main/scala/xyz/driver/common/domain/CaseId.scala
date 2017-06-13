package xyz.driver.common.domain

import java.util.UUID

case class CaseId(id: String)

object CaseId {

  def apply() = new CaseId(UUID.randomUUID().toString)
}
