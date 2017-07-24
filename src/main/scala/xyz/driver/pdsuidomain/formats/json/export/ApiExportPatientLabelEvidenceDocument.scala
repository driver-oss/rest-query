package xyz.driver.pdsuidomain.formats.json.export

import java.time.LocalDate
import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.RecordRequestId
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientLabelEvidenceDocument

final case class ApiExportPatientLabelEvidenceDocument(documentId: String,
                                                       requestId: String,
                                                       documentType: String,
                                                       providerType: String,
                                                       date: LocalDate) {

  def toDomain = ExportPatientLabelEvidenceDocument(
    documentId = LongId(this.documentId.toLong),
    requestId = RecordRequestId(UUID.fromString(this.requestId)),
    documentType = this.documentType,
    providerType = this.providerType,
    date = this.date
  )

}

object ApiExportPatientLabelEvidenceDocument {

  implicit val format: Format[ApiExportPatientLabelEvidenceDocument] = (
    (JsPath \ "documentId").format[String] and
      (JsPath \ "requestId").format[String] and
      (JsPath \ "documentType").format[String] and
      (JsPath \ "providerType").format[String] and
      (JsPath \ "date").format[LocalDate]
  )(ApiExportPatientLabelEvidenceDocument.apply, unlift(ApiExportPatientLabelEvidenceDocument.unapply))

  def fromDomain(document: ExportPatientLabelEvidenceDocument) =
    ApiExportPatientLabelEvidenceDocument(
      documentId = document.documentId.toString,
      requestId = document.requestId.toString,
      documentType = document.documentType,
      providerType = document.providerType,
      date = document.date
    )
}
