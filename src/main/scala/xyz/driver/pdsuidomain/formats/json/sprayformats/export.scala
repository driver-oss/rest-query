package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, Label}
import xyz.driver.pdsuidomain.entities.export.patient._
import xyz.driver.pdsuidomain.entities.export.trial.{ExportTrialArm, ExportTrialLabelCriterion, ExportTrialWithLabels}

object export {
  import DefaultJsonProtocol._
  import common._
  import record._
  import document._

  implicit val patientLabelEvidenceDocumentFormat: RootJsonFormat[ExportPatientLabelEvidenceDocument] =
    jsonFormat5(ExportPatientLabelEvidenceDocument.apply)

  implicit val patientLabelEvidenceFormat: RootJsonFormat[ExportPatientLabelEvidence] =
    jsonFormat(ExportPatientLabelEvidence.apply, "evidenceId", "labelValue", "evidenceText", "document")

  implicit val patientLabelFormat: RootJsonFormat[ExportPatientLabel] =
    jsonFormat(ExportPatientLabel.apply, "labelId", "evidence")

  implicit val patientWithLabelsFormat: RootJsonFormat[ExportPatientWithLabels] =
    jsonFormat(ExportPatientWithLabels.apply, "patientId", "labelVersion", "labels")

  implicit val trialArmFormat: RootJsonFormat[ExportTrialArm] = jsonFormat2(ExportTrialArm.apply)

  implicit val trialLabelCriterionFormat: RootJsonFormat[ExportTrialLabelCriterion] =
    new RootJsonFormat[ExportTrialLabelCriterion] {
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

      override def read(json: JsValue): ExportTrialLabelCriterion = {

        val fields = Seq("value", "labelId", "criterionId", "criterionText", "armIds", "isCompound", "isDefining")

        json.asJsObject.getFields(fields: _*) match {
          case Seq(JsString(valueString),
                   labelId,
                   criterionId,
                   JsString(criterionText),
                   JsArray(armIdsVector),
                   JsBoolean(isCompound),
                   JsBoolean(isDefining)) =>
            val value = valueString match {
              case "Yes"     => Option(true)
              case "No"      => Option(false)
              case "Unknown" => Option.empty[Boolean]
            }

            ExportTrialLabelCriterion(
              longIdFormat[Criterion].read(criterionId),
              value,
              longIdFormat[Label].read(labelId),
              armIdsVector.map(longIdFormat[Arm].read).toSet,
              criterionText,
              isCompound,
              isDefining
            )

          case _ =>
            deserializationError(
              s"Cannot find required fields ${fields.mkString(", ")} in ExportTrialLabelCriterion object!")
        }
      }
    }

  implicit val trialWithLabelsFormat: RootJsonFormat[ExportTrialWithLabels] =
    jsonFormat(ExportTrialWithLabels.apply,
               "nctId",
               "trialId",
               "disease",
               "lastReviewed",
               "labelVersion",
               "arms",
               "criteria")
}
