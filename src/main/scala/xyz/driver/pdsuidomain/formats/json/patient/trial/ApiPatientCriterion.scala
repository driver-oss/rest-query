package xyz.driver.pdsuidomain.formats.json.patient.trial

import java.time.{ZoneId, ZonedDateTime}

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities.{Arm, Label, PatientCriterion}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Reads, Writes}

final case class ApiPatientCriterion(id: Long,
                                     labelId: Long,
                                     nctId: String,
                                     criterionText: String,
                                     criterionValue: Option[String],
                                     criterionIsDefining: Boolean,
                                     criterionIsCompound: Boolean,
                                     arms: List[String],
                                     eligibilityStatus: Option[String],
                                     verifiedEligibilityStatus: Option[String],
                                     isVerified: Boolean,
                                     isVisible: Boolean,
                                     lastUpdate: ZonedDateTime)

object ApiPatientCriterion {

  implicit val format: Format[ApiPatientCriterion] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "labelId").format[Long] and
      (JsPath \ "nctId").format[String] and
      (JsPath \ "criterionText").format[String] and
      (JsPath \ "criterionValue").formatNullable[String](Format(Reads.of[String].filter(ValidationError("unknown value"))({ x =>
        x == "Yes" || x == "No"
      }), Writes.of[String])) and
      (JsPath \ "criterionIsDefining").format[Boolean] and
      (JsPath \ "criterionIsCompound").format[Boolean] and
      (JsPath \ "arms").format[List[String]] and
      (JsPath \ "eligibilityStatus").formatNullable[String](Format(Reads.of[String].filter(ValidationError("unknown status"))({
        case x if FuzzyValue.fromString.isDefinedAt(x) => true
        case _ => false
      }), Writes.of[String])) and
      (JsPath \ "verifiedEligibilityStatus").formatNullable[String](Format(
        Reads.of[String].filter(ValidationError("unknown status"))({
          case x if FuzzyValue.fromString.isDefinedAt(x) => true
          case _ => false
        }), Writes.of[String])) and
      (JsPath \ "isVerified").format[Boolean] and
      (JsPath \ "isVisible").format[Boolean] and
      (JsPath \ "lastUpdate").format[ZonedDateTime]
    ) (ApiPatientCriterion.apply, unlift(ApiPatientCriterion.unapply))

  def fromDomain(patientCriterion: PatientCriterion,
                 labelId: LongId[Label],
                 arms: List[Arm],
                 criterionIsCompound: Boolean) = ApiPatientCriterion(
    id = patientCriterion.id.id,
    labelId = labelId.id,
    nctId = patientCriterion.nctId.id,
    criterionText = patientCriterion.criterionText,
    criterionValue = patientCriterion.criterionValue.map { x =>
      FuzzyValue.valueToString(FuzzyValue.fromBoolean(x))
    },
    criterionIsDefining = patientCriterion.criterionIsDefining,
    criterionIsCompound = criterionIsCompound,
    arms = arms.map(_.name),
    eligibilityStatus = patientCriterion.eligibilityStatus.map(FuzzyValue.valueToString),
    verifiedEligibilityStatus = patientCriterion.verifiedEligibilityStatus.map(FuzzyValue.valueToString),
    isVerified = patientCriterion.isVerified,
    isVisible = patientCriterion.isVisible,
    lastUpdate = ZonedDateTime.of(patientCriterion.lastUpdate, ZoneId.of("Z"))
  )
}
