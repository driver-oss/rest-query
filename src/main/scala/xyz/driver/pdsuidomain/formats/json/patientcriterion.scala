package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.entities.labels.{Label, LabelValue}
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities._

object patientcriterion {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientCriterion(json: JsValue, orig: PatientCriterion): PatientCriterion = json match {
    case JsObject(fields) =>
      val eligibilityStatus = fields
        .get("eligibilityStatus")
        .map(_.convertTo[LabelValue])
        .getOrElse(orig.eligibilityStatus)

      val verifiedEligibilityStatus = fields
        .get("verifiedEligibilityStatus")
        .map(_.convertTo[LabelValue])
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

  implicit val patientCriterionFormat: RootJsonFormat[PatientCriterion]       = jsonFormat14(PatientCriterion.apply)
  implicit val patientCriterionArmFormat: RootJsonFormat[PatientCriterionArm] = jsonFormat3(PatientCriterionArm.apply)

  implicit val richPatientCriterionFormat: RootJsonFormat[RichPatientCriterion] =
    new RootJsonFormat[RichPatientCriterion] {
      override def read(json: JsValue): RichPatientCriterion = {
        val fields  = json.asJsObject.fields
        val labelId = fields.getOrElse("labelId", deserializationError("field 'labelId' is missing"))
        val arms    = fields.getOrElse("armList", deserializationError("field 'arms' is missing"))
        RichPatientCriterion(
          json.convertTo[PatientCriterion],
          labelId.convertTo[LongId[Label]],
          arms.convertTo[List[PatientCriterionArm]]
        )
      }
      override def write(obj: RichPatientCriterion): JsValue = {
        JsObject(
          obj.patientCriterion.toJson.asJsObject.fields ++
            Map(
              "labelId" -> obj.labelId.toJson,
              "armList" -> obj.armList.toJson
            )
        )
      }
    }

}
