package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientCriterionService.DraftPatientCriterion

object patientcriterion {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientCriterion(json: JsValue, orig: PatientCriterion): PatientCriterion = json match {
    case JsObject(fields) =>
      val eligibilityStatus = if (fields.contains("eligibilityStatus")) {
        fields
          .get("eligibilityStatus")
          .map(_.convertTo[FuzzyValue])
      } else orig.eligibilityStatus

      val verifiedEligibilityStatus = if (fields.contains("verifiedEligibilityStatus")) {
        fields
          .get("verifiedEligibilityStatus")
          .map(_.convertTo[FuzzyValue])
      } else orig.verifiedEligibilityStatus

      orig.copy(
        eligibilityStatus = eligibilityStatus,
        verifiedEligibilityStatus = verifiedEligibilityStatus
      )

    case _ => deserializationError(s"Expected Json Object as partial PatientCriterion, but got $json")
  }

  implicit val draftPatientCriterionFormat: RootJsonFormat[DraftPatientCriterion] = jsonFormat3(
    DraftPatientCriterion.apply)
  implicit val draftPatientCriterionListReader = new JsonReader[List[DraftPatientCriterion]] {
    override def read(json: JsValue) = json.convertTo[List[JsValue]].map(_.convertTo[DraftPatientCriterion])
  }

  implicit val patientCriterionWriter: JsonWriter[(PatientCriterion, LongId[Label], List[PatientCriterionArm])] =
    new JsonWriter[(PatientCriterion, LongId[Label], List[PatientCriterionArm])] {
      override def write(obj: (PatientCriterion, LongId[Label], List[PatientCriterionArm])): JsValue = {
        val criterion = obj._1
        val labelId   = obj._2
        val arms      = obj._3
        JsObject(
          "id"            -> criterion.id.toJson,
          "labelId"       -> labelId.toJson,
          "nctId"         -> criterion.nctId.toJson,
          "criterionId"   -> criterion.criterionId.toJson,
          "criterionText" -> criterion.criterionText.toJson,
          "criterionValue" -> criterion.criterionValue.map {
            case true  => "Yes"
            case false => "No"
          }.toJson,
          "criterionIsDefining"       -> criterion.criterionIsDefining.toJson,
          "criterionIsCompound"       -> criterion.criterionValue.isEmpty.toJson,
          "arms"                      -> arms.map(_.armName).toJson,
          "eligibilityStatus"         -> criterion.eligibilityStatus.toJson,
          "verifiedEligibilityStatus" -> criterion.verifiedEligibilityStatus.toJson,
          "isVerified"                -> criterion.isVerified.toJson,
          "isVisible"                 -> criterion.isVisible.toJson,
          "lastUpdate"                -> criterion.lastUpdate.toJson
        )
      }
    }

}
