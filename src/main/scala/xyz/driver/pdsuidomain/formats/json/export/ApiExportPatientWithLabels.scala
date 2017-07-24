package xyz.driver.pdsuidomain.formats.json.export

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientWithLabels

final case class ApiExportPatientWithLabels(patientId: String, labelVersion: Long, labels: List[ApiExportPatientLabel]) {

  def toDomain = ExportPatientWithLabels(
    patientId = UuidId(this.patientId),
    labelVersion = this.labelVersion,
    labels = this.labels.map(_.toDomain)
  )

}

object ApiExportPatientWithLabels {

  implicit val format: Format[ApiExportPatientWithLabels] = (
    (JsPath \ "patientId").format[String] and
      (JsPath \ "labelVersion").format[Long] and
      (JsPath \ "labels").format[List[ApiExportPatientLabel]]
  )(ApiExportPatientWithLabels.apply, unlift(ApiExportPatientWithLabels.unapply))

  def fromDomain(patient: ExportPatientWithLabels) = ApiExportPatientWithLabels(
    patientId = patient.patientId.toString,
    labelVersion = patient.labelVersion,
    labels = patient.labels.map(ApiExportPatientLabel.fromDomain)
  )
}
