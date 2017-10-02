package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, EligibilityArmDisease, EligibilityArmWithDiseases, Trial}

final case class ApiEligibilityArm(id: Long, name: String, originalName: String, trialId: String, diseases: Seq[String]) {

  def toDomain: EligibilityArmWithDiseases = {
    val eligibilityArm = EligibilityArm(
      id = LongId(this.id),
      name = this.name,
      originalName = this.originalName,
      trialId = StringId(this.trialId),
      deleted = None // if we have an ApiEligibilityArm object, the EligibilityArm itself has not been deleted
    )

    EligibilityArmWithDiseases(eligibilityArm, this.diseases.map { disease =>
      val condition = Trial.Condition.fromString(disease)
        .getOrElse(throw new NoSuchElementException(s"unknown condition $disease"))
      EligibilityArmDisease(eligibilityArm.id, condition)
    })
  }
}

object ApiEligibilityArm {

  implicit val format: Format[ApiEligibilityArm] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "originalName").format[String] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "diseases").format[Seq[String]]
  )(ApiEligibilityArm.apply, unlift(ApiEligibilityArm.unapply))

  def fromDomain(eligibilityArmWithDiseases: EligibilityArmWithDiseases): ApiEligibilityArm = {
    import eligibilityArmWithDiseases.{eligibilityArm, eligibilityArmDiseases}

    ApiEligibilityArm(
      id = eligibilityArm.id.id,
      name = eligibilityArm.name,
      originalName = eligibilityArm.originalName,
      trialId = eligibilityArm.trialId.id,
      diseases = eligibilityArmDiseases.map(_.disease.toString)
    )
  }
}
