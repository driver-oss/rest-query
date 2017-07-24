package xyz.driver.pdsuidomain.formats.json.export

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientLabelEvidence

final case class ApiExportPatientLabelEvidence(evidenceId: String,
                                               labelValue: String,
                                               evidenceText: String,
                                               document: ApiExportPatientLabelEvidenceDocument) {

  def toDomain = ExportPatientLabelEvidence(
    id = LongId(this.evidenceId.toLong),
    value = FuzzyValue.fromString
      .applyOrElse(this.labelValue, (s: String) => throw new NoSuchElementException(s"Unknown fuzzy value $s")),
    evidenceText = this.evidenceText,
    document = this.document.toDomain
  )

}

object ApiExportPatientLabelEvidence {

  implicit val format: Format[ApiExportPatientLabelEvidence] = (
    (JsPath \ "evidenceId").format[String] and
      (JsPath \ "labelValue").format[String](Writes[String](x => JsString(x.toUpperCase))) and
      (JsPath \ "evidenceText").format[String] and
      (JsPath \ "document").format[ApiExportPatientLabelEvidenceDocument]
  )(ApiExportPatientLabelEvidence.apply, unlift(ApiExportPatientLabelEvidence.unapply))

  def fromDomain(evidence: ExportPatientLabelEvidence) = ApiExportPatientLabelEvidence(
    evidenceId = evidence.id.toString,
    labelValue = FuzzyValue.valueToString(evidence.value),
    evidenceText = evidence.evidenceText,
    document = ApiExportPatientLabelEvidenceDocument.fromDomain(evidence.document)
  )
}
