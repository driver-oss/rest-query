package xyz.driver.pdsuidomain.formats.json.slotarm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.SlotArm
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiSlotArm(id: Long, name: String, originalName: String, trialId: String) {

  def toDomain: SlotArm = SlotArm(
    id = LongId(this.id),
    name = this.name,
    originalName = this.originalName,
    trialId = StringId(this.trialId),
    deleted = None // if we have an ApiSlotArm object, the SlotArm itself has not been deleted
  )

}

object ApiSlotArm {

  implicit val format: Format[ApiSlotArm] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "originalName").format[String] and
      (JsPath \ "trialId").format[String]
  )(ApiSlotArm.apply, unlift(ApiSlotArm.unapply))

  def fromDomain(arm: SlotArm): ApiSlotArm = ApiSlotArm(
    id = arm.id.id,
    name = arm.name,
    originalName = arm.originalName,
    trialId = arm.trialId.id
  )
}
