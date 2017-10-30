package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientCriterionService.{DraftPatientCriterion, RichPatientCriterion}

object patientcriterion {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientCriterion(json: JsValue, orig: PatientCriterion): PatientCriterion = json match {
    case JsObject(fields) =>
      val eligibilityStatus = fields
        .get("eligibilityStatus")
        .map(_.convertTo[Option[LabelValue]])
        .getOrElse(orig.eligibilityStatus)

      val verifiedEligibilityStatus = fields
        .get("verifiedEligibilityStatus")
        .map(_.convertTo[Option[LabelValue]])
        .getOrElse(orig.verifiedEligibilityStatus)

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

  implicit val patientCriterionWriter: RootJsonWriter[RichPatientCriterion] =
    new RootJsonWriter[RichPatientCriterion] {
      override def write(obj: RichPatientCriterion): JsValue = {
        JsObject(
          "id"            -> obj.patientCriterion.id.toJson,
          "labelId"       -> obj.labelId.toJson,
          "nctId"         -> obj.patientCriterion.nctId.toJson,
          "criterionId"   -> obj.patientCriterion.criterionId.toJson,
          "criterionText" -> obj.patientCriterion.criterionText.toJson,
          "criterionValue" -> obj.patientCriterion.criterionValue.map {
            case true  => "Yes"
            case false => "No"
          }.toJson,
          "criterionIsDefining"       -> obj.patientCriterion.criterionIsDefining.toJson,
          "criterionIsCompound"       -> obj.patientCriterion.criterionValue.isEmpty.toJson,
          "arms"                      -> obj.armList.map(_.armName).toJson,
          "eligibilityStatus"         -> obj.patientCriterion.eligibilityStatus.toJson,
          "verifiedEligibilityStatus" -> obj.patientCriterion.verifiedEligibilityStatus.toJson,
          "isVerified"                -> obj.patientCriterion.isVerified.toJson,
          "isVisible"                 -> obj.patientCriterion.isVisible.toJson,
          "lastUpdate"                -> obj.patientCriterion.lastUpdate.toJson,
          "inclusion"                 -> obj.patientCriterion.inclusion.toJson
        )
      }
    }

}