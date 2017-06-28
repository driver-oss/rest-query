package xyz.driver.pdsuidomain.formats.json.patient.trial

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities.PatientCriterion
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Reads, Writes}
import xyz.driver.pdsuidomain.services.PatientCriterionService.DraftPatientCriterion

final case class ApiPartialPatientCriterionList(id: Long,
                                          eligibilityStatus: Option[String],
                                          isVerified: Option[Boolean]) {

  def toDomain: DraftPatientCriterion = DraftPatientCriterion(
    id = LongId[PatientCriterion](id),
    eligibilityStatus = eligibilityStatus.map(FuzzyValue.fromString),
    isVerified = isVerified
  )
}

object ApiPartialPatientCriterionList {

  implicit val format: Format[ApiPartialPatientCriterionList] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "eligibilityStatus").formatNullable[String](Format(
        Reads.of[String].filter(ValidationError("unknown eligibility status"))({
        case x if FuzzyValue.fromString.isDefinedAt(x) => true
        case _ => false
      }), Writes.of[String])) and
      (JsPath \ "isVerified").formatNullable[Boolean]
    ) (ApiPartialPatientCriterionList.apply, unlift(ApiPartialPatientCriterionList.unapply))
}
