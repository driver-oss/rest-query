package xyz.driver.pdsuidomain.formats.json.slotarm


import xyz.driver.pdsuidomain.entities.SlotArm
import play.api.libs.json.{Format, Json}

final case class ApiPartialSlotArm(name: String) {

  def applyTo(arm: SlotArm): SlotArm = arm.copy(name = name)
}

object ApiPartialSlotArm {

  implicit val format: Format[ApiPartialSlotArm] = Json.format
}
