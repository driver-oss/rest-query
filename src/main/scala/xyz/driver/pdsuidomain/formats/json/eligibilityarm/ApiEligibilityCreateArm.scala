package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, EligibilityArmDisease, EligibilityArmWithDiseases, Trial}
import play.api.libs.json.{Format, Json}

final case class ApiCreateEligibilityArm(name: String, trialId: String, diseases: Seq[String]) {

  def toDomain: EligibilityArmWithDiseases = {
    val eligibilityArm = EligibilityArm(
      id = LongId(0),
      name = name,
      trialId = StringId(trialId),
      originalName = name
    )

    EligibilityArmWithDiseases(eligibilityArm, diseases.map { disease =>
      val condition = Trial.Condition.fromString(disease)
        .getOrElse(throw new NoSuchElementException(s"unknown condition $disease"))
      EligibilityArmDisease(eligibilityArm.id, condition)
    })
  }
}

object ApiCreateEligibilityArm {

  implicit val format: Format[ApiCreateEligibilityArm] = Json.format[ApiCreateEligibilityArm]
}
