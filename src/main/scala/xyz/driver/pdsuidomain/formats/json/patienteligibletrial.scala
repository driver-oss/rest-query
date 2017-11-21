package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial

object patienteligibletrial {
  import DefaultJsonProtocol._
  import common._
  import xyz.driver.pdsuidomain.formats.json.trial._
  import xyz.driver.pdsuidomain.formats.json.patientcriterion._

  def applyUpdateToTrialArmGroup(json: JsValue, orig: PatientTrialArmGroupView): PatientTrialArmGroupView =
    json match {
      case JsObject(fields) =>
        val isVerified = fields
          .get("isVerified")
          .map(_.convertTo[Boolean])
          .getOrElse(orig.isVerified)

        orig.copy(isVerified = isVerified)

      case _ => deserializationError(s"Expected Json Object as partial PatientTrialArmGroupView, but got $json")
    }

  implicit val patientEligibleArmGroupView: RootJsonFormat[PatientTrialArmGroupView] =
    jsonFormat7(PatientTrialArmGroupView.apply)

  implicit val patientEligibleTrialFormat: RootJsonFormat[RichPatientEligibleTrial] =
    jsonFormat3(RichPatientEligibleTrial.apply)

}
