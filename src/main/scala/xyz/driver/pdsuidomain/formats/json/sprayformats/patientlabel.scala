package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.FuzzyValue
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel

object patientlabel {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientLabel(json: JsValue, orig: PatientLabel): PatientLabel = json match {
    case JsObject(fields) =>
      val primaryValue = fields
        .get("primaryValue")
        .map(_.convertTo[Option[FuzzyValue]])
        .getOrElse(orig.primaryValue)

      val verifiedPrimaryValue = fields
        .get("verifiedPrimaryValue")
        .map(_.convertTo[Option[FuzzyValue]])
        .getOrElse(orig.verifiedPrimaryValue)

      orig.copy(
        primaryValue = primaryValue,
        verifiedPrimaryValue = verifiedPrimaryValue
      )

    case _ => deserializationError(s"Expected Json Object as PatientLabel, but got $json")
  }

  implicit val patientLabelWriter: JsonWriter[RichPatientLabel] = new JsonWriter[RichPatientLabel] {
    override def write(obj: RichPatientLabel): JsValue = {
      JsObject(
        "id"                   -> obj.patientLabel.id.toJson,
        "labelId"              -> obj.patientLabel.labelId.toJson,
        "primaryValue"         -> obj.patientLabel.primaryValue.toJson,
        "verifiedPrimaryValue" -> obj.patientLabel.verifiedPrimaryValue.toJson,
        "score"                -> obj.patientLabel.score.toJson,
        "isImplicitMatch"      -> obj.patientLabel.isImplicitMatch.toJson,
        "isVisible"            -> obj.patientLabel.isVisible.toJson,
        "isVerified"           -> obj.isVerified.toJson
      )
    }
  }

  implicit val patientLabelEvidenceWriter: JsonWriter[PatientLabelEvidenceView] =
    new JsonWriter[PatientLabelEvidenceView] {
      override def write(evidence: PatientLabelEvidenceView): JsValue =
        JsObject(
          "id"           -> evidence.id.toJson,
          "value"        -> evidence.value.toJson,
          "evidenceText" -> evidence.evidenceText.toJson,
          "documentId"   -> evidence.documentId.toJson,
          "evidenceId"   -> evidence.evidenceId.toJson,
          "reportId"     -> evidence.reportId.toJson,
          "documentType" -> evidence.documentType.toJson,
          "date"         -> evidence.date.toJson,
          "providerType" -> evidence.providerType.toJson
        )
    }

}
