package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.FuzzyValue
import xyz.driver.pdsuidomain.entities._

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

  implicit val patientLabelWriter: JsonWriter[(PatientLabel, Boolean)] = new JsonWriter[(PatientLabel, Boolean)] {
    override def write(obj: (PatientLabel, Boolean)): JsValue = {
      val patientLabel = obj._1
      val isVerified   = obj._2
      JsObject(
        "id"                   -> patientLabel.id.toJson,
        "labelId"              -> patientLabel.labelId.toJson,
        "primaryValue"         -> patientLabel.primaryValue.toJson,
        "verifiedPrimaryValue" -> patientLabel.verifiedPrimaryValue.toJson,
        "score"                -> patientLabel.score.toJson,
        "isImplicitMatch"      -> patientLabel.isImplicitMatch.toJson,
        "isVisible"            -> patientLabel.isVisible.toJson,
        "isVerified"           -> isVerified.toJson
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
