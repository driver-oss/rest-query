package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.entities.labels.Label
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.export.patient._
import xyz.driver.pdsuidomain.entities.export.trial.{ExportTrialArm, ExportTrialLabelCriterion, ExportTrialWithLabels}
import xyz.driver.pdsuidomain.entities.{Criterion, EligibilityArm}

object export {
  import DefaultJsonProtocol._
  import common._
  import document._
  import record._

  private def deserializationErrorFieldMessage(field: String, json: JsValue)(implicit className: String) = {
    deserializationError(s"$className json object do not contain '$field' field: $json")
  }

  private def deserializationErrorEntityMessage(json: JsValue)(implicit className: String) = {
    deserializationError(s"Expected Json Object as $className, but got $json")
  }

  implicit val patientLabelEvidenceDocumentFormat: RootJsonFormat[ExportPatientLabelEvidenceDocument] =
    jsonFormat5(ExportPatientLabelEvidenceDocument.apply)

  implicit val patientLabelEvidenceFormat: RootJsonFormat[ExportPatientLabelEvidence] =
    jsonFormat(ExportPatientLabelEvidence.apply, "evidenceId", "labelValue", "evidenceText", "document")

  implicit val patientLabelFormat: RootJsonFormat[ExportPatientLabel] =
    jsonFormat(ExportPatientLabel.apply, "labelId", "evidence")

  implicit val patientWithLabelsFormat: RootJsonFormat[ExportPatientWithLabels] =
    jsonFormat(ExportPatientWithLabels.apply, "patientId", "labelVersion", "labels")

  implicit val trialArmFormat: RootJsonFormat[ExportTrialArm] = jsonFormat3(ExportTrialArm.apply)

  implicit val trialLabelCriterionFormat: RootJsonFormat[ExportTrialLabelCriterion] =
    new RootJsonFormat[ExportTrialLabelCriterion] {
      implicit val className: String = "ExportTrialLabelCriterion"

      override def write(obj: ExportTrialLabelCriterion): JsValue =
        JsObject(
          "value" -> obj.value
            .map {
              case true  => "Yes"
              case false => "No"
            }
            .getOrElse("Unknown")
            .toJson,
          "labelId"       -> obj.labelId.toJson,
          "criterionId"   -> obj.criterionId.toJson,
          "criterionText" -> obj.criteria.toJson,
          "armIds"        -> obj.armIds.toJson,
          "isCompound"    -> obj.isCompound.toJson,
          "isDefining"    -> obj.isDefining.toJson,
          "inclusion"     -> obj.inclusion.toJson
        )

      override def read(json: JsValue): ExportTrialLabelCriterion = {
        json match {
          case JsObject(fields) =>
            val value = fields
              .get("value")
              .map(_.convertTo[String])
              .map {
                case "Yes"     => Option(true)
                case "No"      => Option(false)
                case "Unknown" => Option.empty[Boolean]
              }
              .getOrElse(deserializationErrorFieldMessage("value", json))

            val labelId = fields
              .get("labelId")
              .map(_.convertTo[LongId[Label]])
              .getOrElse(deserializationErrorFieldMessage("labelId", json))

            val criterionId = fields
              .get("criterionId")
              .map(_.convertTo[LongId[Criterion]])
              .getOrElse(deserializationErrorFieldMessage("criterionId", json))

            val criterionText = fields
              .get("criterionText")
              .map(_.convertTo[String])
              .getOrElse(deserializationErrorFieldMessage("criterionText", json))

            val armIds = fields
              .get("armIds")
              .map(_.convertTo[Set[LongId[EligibilityArm]]])
              .getOrElse(deserializationErrorFieldMessage("armIds", json))

            val isCompound = fields
              .get("isCompound")
              .map(_.convertTo[Boolean])
              .getOrElse(deserializationErrorFieldMessage("isCompound", json))

            val isDefining = fields
              .get("isDefining")
              .map(_.convertTo[Boolean])
              .getOrElse(deserializationErrorFieldMessage("isDefining", json))

            val inclusion = fields
              .get("inclusion")
              .flatMap(_.convertTo[Option[Boolean]])

            ExportTrialLabelCriterion(
              criterionId,
              value,
              labelId,
              armIds,
              criterionText,
              isCompound,
              isDefining,
              inclusion
            )

          case _ => deserializationErrorEntityMessage(json)
        }
      }
    }

  implicit val trialWithLabelsFormat: RootJsonFormat[ExportTrialWithLabels] =
    jsonFormat(ExportTrialWithLabels.apply, "nctId", "trialId", "lastReviewed", "labelVersion", "arms", "criteria")
}
