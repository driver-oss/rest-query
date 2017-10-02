package xyz.driver.pdsuidomain.formats.json.eligibilityarm

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.{EligibilityArmDisease, EligibilityArmWithDiseases, Trial}

final case class ApiPartialEligibilityArm(name: String, diseases: Seq[String]) {

  def applyTo(armWithDisease: EligibilityArmWithDiseases): EligibilityArmWithDiseases = {
    val arm = armWithDisease.eligibilityArm.copy(name = name)
    val armDiseases = diseases.map { disease =>
      EligibilityArmDisease(
        armWithDisease.eligibilityArm.id,
        Trial.Condition.fromString(disease).getOrElse(throw new NoSuchElementException(s"unknown condition $disease")))
    }
    EligibilityArmWithDiseases(arm ,armDiseases)
  }
}

object ApiPartialEligibilityArm {

  implicit val format: Format[ApiPartialEligibilityArm] = Json.format
}
