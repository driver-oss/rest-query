package xyz.driver.pdsuidomain.formats.json.arm

import xyz.driver.pdsuidomain.entities.Arm
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiArm(id: Long, name: String, trialId: String)

object ApiArm {

  implicit val format: Format[ApiArm] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "trialId").format[String]
    ) (ApiArm.apply, unlift(ApiArm.unapply))

  def fromDomain(arm: Arm): ApiArm = ApiArm(
    id = arm.id.id,
    name = arm.name,
    trialId = arm.trialId.id
  )
}
