package xyz.driver.pdsuidomain.formats.json.arm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.Arm
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiArm(id: Long, name: String, originalName: String, trialId: String) {

  def toDomain: Arm = Arm(
    id = LongId(this.id),
    name = this.name,
    originalName = this.originalName,
    trialId = StringId(this.trialId),
    deleted = None // if we have an ApiArm object, the Arm itself has not been deleted
  )

}

object ApiArm {

  implicit val format: Format[ApiArm] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "originalName").format[String] and
      (JsPath \ "trialId").format[String]
  )(ApiArm.apply, unlift(ApiArm.unapply))

  def fromDomain(arm: Arm): ApiArm = ApiArm(
    id = arm.id.id,
    name = arm.name,
    originalName = arm.originalName,
    trialId = arm.trialId.id
  )
}
