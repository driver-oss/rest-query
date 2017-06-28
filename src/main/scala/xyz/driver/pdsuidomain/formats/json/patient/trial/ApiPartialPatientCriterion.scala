package xyz.driver.pdsuidomain.formats.json.patient.trial

import xyz.driver.pdsuidomain.entities.PatientCriterion
import org.davidbild.tristate.Tristate
import org.davidbild.tristate.contrib.play.ToJsPathOpsFromJsPath
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Reads, Writes}
import xyz.driver.pdsuicommon.domain.FuzzyValue

final case class ApiPartialPatientCriterion(eligibilityStatus: Option[String],
                                            verifiedEligibilityStatus: Tristate[String])  {

  def applyTo(orig: PatientCriterion): PatientCriterion = {
    orig.copy(
      eligibilityStatus = eligibilityStatus.map(FuzzyValue.fromString).orElse(orig.eligibilityStatus),
      verifiedEligibilityStatus = verifiedEligibilityStatus.cata(x =>
        Some(FuzzyValue.fromString(x)),
        None,
        orig.verifiedEligibilityStatus
      )
    )
  }
}

object ApiPartialPatientCriterion {

  implicit val format: Format[ApiPartialPatientCriterion] = (
    (JsPath \ "eligibilityStatus").formatNullable[String](Format(
      Reads.of[String].filter(ValidationError("unknown eligibility status"))({
        case x if FuzzyValue.fromString.isDefinedAt(x) => true
        case _ => false
      }), Writes.of[String])) and
      (JsPath \ "verifiedEligibilityStatus").formatTristate[String](Format(
        Reads.of[String].filter(ValidationError("unknown verified eligibility status"))({
          case x if FuzzyValue.fromString.isDefinedAt(x) => true
          case _ => false
        }), Writes.of[String]))
    ) (ApiPartialPatientCriterion.apply, unlift(ApiPartialPatientCriterion.unapply))
}
