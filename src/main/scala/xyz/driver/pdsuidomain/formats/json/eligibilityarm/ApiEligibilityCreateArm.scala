package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.EligibilityArm
import play.api.libs.json.{Format, Json}

final case class ApiCreateEligibilityArm(name: String, trialId: String) {

  def toDomain = EligibilityArm(
    id = LongId(0),
    name = name,
    trialId = StringId(trialId),
    originalName = name
  )
}

object ApiCreateEligibilityArm {

  implicit val format: Format[ApiCreateEligibilityArm] = Json.format[ApiCreateEligibilityArm]
}
