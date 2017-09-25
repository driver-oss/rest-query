package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import xyz.driver.pdsuidomain.entities.EligibilityArm

final case class ApiPartialEligibilityArm(name: String) {

  def applyTo(arm: EligibilityArm): EligibilityArm = arm.copy(name = name)
}

object ApiPartialEligibilityArm {

  implicit val format: Format[ApiPartialEligibilityArm] = Json.format
}
