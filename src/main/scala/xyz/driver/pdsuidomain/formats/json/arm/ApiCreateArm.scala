package xyz.driver.pdsuidomain.formats.json.arm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.Arm
import play.api.libs.json.{Format, Json}

final case class ApiCreateArm(name: String, trialId: String) {

  def toDomain = Arm(
    id = LongId(0),
    name = name,
    trialId = StringId(trialId),
    originalName = name
  )
}

object ApiCreateArm {

  implicit val format: Format[ApiCreateArm] = Json.format[ApiCreateArm]
}
