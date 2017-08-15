package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities.export.patient._
import xyz.driver.pdsuidomain.entities.export.trial.{ExportTrialArm, ExportTrialLabelCriterion, ExportTrialWithLabels}

object export {
  import DefaultJsonProtocol._
  import common._
  import record._

  implicit val patientLabelEvidenceDocumentFormat: RootJsonFormat[ExportPatientLabelEvidenceDocument] = jsonFormat5(
    ExportPatientLabelEvidenceDocument.apply)

  implicit val patientLabelEvidenceWriter: JsonWriter[ExportPatientLabelEvidence] =
    new JsonWriter[ExportPatientLabelEvidence] {
      override def write(obj: ExportPatientLabelEvidence): JsValue =
        JsObject(
          "evidenceId"   -> obj.id.toJson,
          "labelValue"   -> obj.value.toJson,
          "evidenceText" -> obj.evidenceText.toJson,
          "document"     -> obj.document.toJson
        )
    }

  implicit val patientLabelWriter: JsonWriter[ExportPatientLabel] = new JsonWriter[ExportPatientLabel] {
    override def write(obj: ExportPatientLabel): JsValue =
      JsObject(
        "labelId"  -> obj.id.toJson,
        "evidence" -> obj.evidences.map(_.toJson).toJson
      )
  }

  implicit val patientWithLabelsWriter: JsonWriter[ExportPatientWithLabels] = new JsonWriter[ExportPatientWithLabels] {
    override def write(obj: ExportPatientWithLabels): JsValue =
      JsObject(
        "patientId"    -> obj.patientId.toJson,
        "labelVersion" -> obj.labelVersion.toJson,
        "labels"       -> obj.labels.map(_.toJson).toJson
      )
  }

  implicit val trialArmFormat: RootJsonFormat[ExportTrialArm] = jsonFormat2(ExportTrialArm.apply)

  implicit val trialLabelCriterionWriter: JsonWriter[ExportTrialLabelCriterion] =
    new JsonWriter[ExportTrialLabelCriterion] {
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
          "isDefining"    -> obj.isDefining.toJson
        )
    }

  implicit val trialWithLabelsWriter: JsonWriter[ExportTrialWithLabels] = new JsonWriter[ExportTrialWithLabels] {
    override def write(obj: ExportTrialWithLabels) =
      JsObject(
        "nctId"        -> obj.nctId.toJson,
        "trialId"      -> obj.trialId.toJson,
        "disease"      -> obj.condition.toJson,
        "lastReviewed" -> obj.lastReviewed.toJson,
        "labelVersion" -> obj.labelVersion.toJson,
        "arms"         -> obj.arms.toJson,
        "criteria"     -> obj.criteria.map(_.toJson).toJson
      )
  }

}
