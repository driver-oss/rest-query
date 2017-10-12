package xyz.driver.pdsuidomain.formats.json.slotarm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.SlotArm
import play.api.libs.json.{Format, Json}

final case class ApiCreateSlotArm(name: String, trialId: String) {

  def toDomain = SlotArm(
    id = LongId(0),
    name = name,
    trialId = StringId(trialId),
    originalName = name
  )
}

object ApiCreateSlotArm {

  implicit val format: Format[ApiCreateSlotArm] = Json.format[ApiCreateSlotArm]
}
