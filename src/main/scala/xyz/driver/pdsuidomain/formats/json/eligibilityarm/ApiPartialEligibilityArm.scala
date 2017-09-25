package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import xyz.driver.pdsuidomain.entities.EligibilityArm
import play.api.libs.json.{Format, Json}

final case class ApiPartialEligibilityArm(name: String) {

  def applyTo(arm: EligibilityArm): EligibilityArm = arm.copy(name = name)
}

object ApiPartialEligibilityArm {

  implicit val format: Format[ApiPartialEligibilityArm] = Json.format
}
