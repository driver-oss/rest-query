package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.EligibilityArm
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiEligibilityArm(id: Long, name: String, originalName: String, trialId: String) {

  def toDomain: EligibilityArm = EligibilityArm(
    id = LongId(this.id),
    name = this.name,
    originalName = this.originalName,
    trialId = StringId(this.trialId),
    deleted = None // if we have an ApiEligibilityArm object, the EligibilityArm itself has not been deleted
  )

}

object ApiEligibilityArm {

  implicit val format: Format[ApiEligibilityArm] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "originalName").format[String] and
      (JsPath \ "trialId").format[String]
    )(ApiEligibilityArm.apply, unlift(ApiEligibilityArm.unapply))

  def fromDomain(arm: EligibilityArm): ApiEligibilityArm = ApiEligibilityArm(
    id = arm.id.id,
    name = arm.name,
    originalName = arm.originalName,
    trialId = arm.trialId.id
  )
}