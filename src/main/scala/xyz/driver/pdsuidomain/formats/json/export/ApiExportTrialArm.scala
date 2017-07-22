package xyz.driver.pdsuidomain.formats.json.export

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.Arm
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrialArm

final case class ApiExportTrialArm(armId: String, armName: String) {

  def toDomain: ExportTrialArm = {
    ExportTrialArm(LongId[Arm](armId.toLong), armName)
  }
}

object ApiExportTrialArm {

  implicit val format: Format[ApiExportTrialArm] = (
    (JsPath \ "armId").format[String] and
      (JsPath \ "armName").format[String]
  )(ApiExportTrialArm.apply, unlift(ApiExportTrialArm.unapply))

  def fromDomain(arm: ExportTrialArm) = ApiExportTrialArm(
    armId = arm.armId.toString,
    armName = arm.armName
  )
}
