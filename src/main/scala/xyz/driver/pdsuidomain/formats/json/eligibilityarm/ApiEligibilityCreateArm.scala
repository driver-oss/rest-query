package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import play.api.libs.json.{Format, Json}
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, EligibilityArmDisease, EligibilityArmWithDiseases}

final case class ApiCreateEligibilityArm(name: String, trialId: String, diseases: Seq[String]) {

  def toDomain: EligibilityArmWithDiseases = {
    val eligibilityArm = EligibilityArm(
      id = LongId(0),
      name = name,
      trialId = StringId(trialId),
      originalName = name
    )

    EligibilityArmWithDiseases(
      eligibilityArm,
      diseases.map { disease =>
        val condition = CancerType
          .fromString(disease)
          .getOrElse(throw new NoSuchElementException(s"unknown condition $disease"))
        EligibilityArmDisease(eligibilityArm.id, condition)
      }
    )
  }
}

object ApiCreateEligibilityArm {

  implicit val format: Format[ApiCreateEligibilityArm] = Json.format[ApiCreateEligibilityArm]
}
