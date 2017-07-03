package xyz.driver.pdsuidomain.formats.json.patient.eligible

import java.util.UUID

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.FuzzyValue
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial

final case class ApiPatientEligibleTrial(id: Long,
                                         patientId: String,
                                         trialId: String,
                                         trialTitle: String,
                                         arms: List[String],
                                         hypothesisId: UUID,
                                         verifiedEligibilityStatus: Option[String],
                                         isVerified: Boolean)

object ApiPatientEligibleTrial {

  implicit val apiEligibleTrialJsonFormat: Format[ApiPatientEligibleTrial] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "patientId").format[String] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "trialTitle").format[String] and
      (JsPath \ "arms").format[List[String]] and
      (JsPath \ "hypothesisId").format[UUID] and
      (JsPath \ "verifiedEligibilityStatus").formatNullable[String](Format(
        Reads
          .of[String]
          .filter(ValidationError("unknown eligibility status"))({
            case x if FuzzyValue.fromString.isDefinedAt(x) => true
            case _                                         => false
          }),
        Writes.of[String]
      )) and
      (JsPath \ "isVerified").format[Boolean]
  )(ApiPatientEligibleTrial.apply, unlift(ApiPatientEligibleTrial.unapply))

  def fromDomain(eligibleTrialWithTrial: RichPatientEligibleTrial) = ApiPatientEligibleTrial(
    id = eligibleTrialWithTrial.group.id.id,
    patientId = eligibleTrialWithTrial.group.patientId.toString,
    trialId = eligibleTrialWithTrial.group.trialId.id,
    trialTitle = eligibleTrialWithTrial.trial.title,
    arms = eligibleTrialWithTrial.arms.map(_.armName),
    hypothesisId = eligibleTrialWithTrial.group.hypothesisId.id,
    eligibleTrialWithTrial.group.verifiedEligibilityStatus.map(FuzzyValue.valueToString),
    eligibleTrialWithTrial.group.isVerified
  )
}
