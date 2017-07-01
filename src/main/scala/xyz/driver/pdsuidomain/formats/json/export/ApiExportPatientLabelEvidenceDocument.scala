package xyz.driver.pdsuidomain.formats.json.export

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientLabelEvidenceDocument

final case class ApiExportPatientLabelEvidenceDocument(documentId: String,
                                                       requestId: String,
                                                       documentType: String,
                                                       providerType: String,
                                                       date: LocalDate)


object ApiExportPatientLabelEvidenceDocument {

  implicit val format: Format[ApiExportPatientLabelEvidenceDocument] = (
    (JsPath \ "documentId").format[String] and
      (JsPath \ "requestId").format[String] and
      (JsPath \ "documentType").format[String] and
      (JsPath \ "providerType").format[String] and
      (JsPath \ "date").format[LocalDate]
    ) (ApiExportPatientLabelEvidenceDocument.apply, unlift(ApiExportPatientLabelEvidenceDocument.unapply))

  def fromDomain(document: ExportPatientLabelEvidenceDocument) =
    ApiExportPatientLabelEvidenceDocument(
      documentId = document.documentId.toString,
      requestId = document.requestId.toString,
      documentType = document.documentType,
      providerType = document.providerType,
      date = document.date
    )
}
