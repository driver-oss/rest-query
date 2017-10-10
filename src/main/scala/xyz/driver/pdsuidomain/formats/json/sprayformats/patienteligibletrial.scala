package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial
import xyz.driver.formats.json.labels._

object patienteligibletrial {
  import DefaultJsonProtocol._
  import common._

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

  implicit val patientEligibleTrialWriter: RootJsonWriter[RichPatientEligibleTrial] =
    new RootJsonWriter[RichPatientEligibleTrial] {
      override def write(obj: RichPatientEligibleTrial) =
        JsObject(
          "id"                        -> obj.group.id.toJson,
          "patientId"                 -> obj.group.patientId.toJson,
          "trialId"                   -> obj.group.trialId.toJson,
          "trialTitle"                -> obj.trial.title.toJson,
          "arms"                      -> obj.arms.map(_.armName).toJson,
          "hypothesisId"              -> obj.trial.hypothesisId.toJson,
          "verifiedEligibilityStatus" -> obj.group.verifiedEligibilityStatus.toJson,
          "isVerified"                -> obj.group.isVerified.toJson
        )
    }

}
