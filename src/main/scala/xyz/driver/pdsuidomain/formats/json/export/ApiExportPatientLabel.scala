package xyz.driver.pdsuidomain.formats.json.export

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientLabel

final case class ApiExportPatientLabel(id: String, labelName: String, evidences: List[ApiExportPatientLabelEvidence])

object ApiExportPatientLabel {

  implicit val format: Format[ApiExportPatientLabel] = (
    (JsPath \ "labelId").format[String] and
      (JsPath \ "labelName").format[String] and
      (JsPath \ "evidence").format[List[ApiExportPatientLabelEvidence]]
    ) (ApiExportPatientLabel.apply, unlift(ApiExportPatientLabel.unapply))

  def fromDomain(label: ExportPatientLabel) = ApiExportPatientLabel(
    id = label.id.toString,
    labelName = label.name,
    evidences = label.evidences.map(ApiExportPatientLabelEvidence.fromDomain)
  )
}
