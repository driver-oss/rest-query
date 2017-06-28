package xyz.driver.pdsuidomain.formats.json.patient.eligible

import xyz.driver.pdsuidomain.entities.PatientTrialArmGroupView
import play.api.libs.json.{Format, Json}

final case class ApiPartialPatientEligibleTrial(isVerified: Option[Boolean]) {

  def applyTo(orig: PatientTrialArmGroupView): PatientTrialArmGroupView = {
    orig.copy(
      isVerified = isVerified.getOrElse(orig.isVerified)
    )
  }
}

object ApiPartialPatientEligibleTrial {

  implicit val format: Format[ApiPartialPatientEligibleTrial] = Json.format
}
