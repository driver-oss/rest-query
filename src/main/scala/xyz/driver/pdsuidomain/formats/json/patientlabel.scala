package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuidomain.formats.json.record._
import xyz.driver.pdsuidomain.formats.json.document._

object patientlabel {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientLabel(json: JsValue, orig: PatientLabel): PatientLabel = json match {
    case JsObject(fields) =>
      val primaryValue = fields
        .get("primaryValue")
        .map(_.convertTo[LabelValue])
        .getOrElse(orig.primaryValue)

      val verifiedPrimaryValue = fields
        .get("verifiedPrimaryValue")
        .map(_.convertTo[LabelValue])
        .getOrElse(orig.verifiedPrimaryValue)

      orig.copy(
        primaryValue = primaryValue,
        verifiedPrimaryValue = verifiedPrimaryValue
      )

    case _ => deserializationError(s"Expected Json Object as PatientLabel, but got $json")
  }

  implicit val patientLabelFormat: RootJsonFormat[PatientLabel] = jsonFormat8(PatientLabel.apply)

  implicit val richPatientLabelFormat: RootJsonFormat[RichPatientLabel] = new RootJsonFormat[RichPatientLabel] {
    override def read(json: JsValue): RichPatientLabel = {
      val isVerified =
        json.asJsObject.fields.getOrElse("isVerified", deserializationError("isVerified field is missing"))
      RichPatientLabel(json.convertTo[PatientLabel], isVerified.convertTo[Boolean])
    }
    override def write(obj: RichPatientLabel): JsValue = {
      val labelFields = obj.patientLabel.toJson.asJsObject.fields
      JsObject(labelFields ++ Map("isVerified" -> obj.isVerified.toJson))
    }
  }

  implicit val patientLabelEvidenceWriter: RootJsonWriter[PatientLabelEvidenceView] =
    new RootJsonWriter[PatientLabelEvidenceView] {
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
