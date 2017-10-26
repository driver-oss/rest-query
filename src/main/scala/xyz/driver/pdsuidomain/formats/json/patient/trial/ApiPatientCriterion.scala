package xyz.driver.pdsuidomain.formats.json.patient.trial

import java.time.{ZoneId, ZonedDateTime}

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.{PatientCriterion, PatientCriterionArm}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Reads, Writes}
import xyz.driver.entities.labels.{Label, LabelValue}

final case class ApiPatientCriterion(id: Long,
                                     labelId: Long,
                                     nctId: String,
                                     criterionId: Long,
                                     criterionText: String,
                                     criterionValue: Option[String],
                                     criterionIsDefining: Boolean,
                                     criterionIsCompound: Boolean,
                                     arms: List[String],
                                     eligibilityStatus: Option[String],
                                     verifiedEligibilityStatus: Option[String],
                                     isVerified: Boolean,
                                     isVisible: Boolean,
                                     lastUpdate: ZonedDateTime,
                                     inclusion: Option[Boolean])

object ApiPatientCriterion {

  implicit val format: Format[ApiPatientCriterion] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "labelId").format[Long] and
      (JsPath \ "nctId").format[String] and
      (JsPath \ "criterionId").format[Long] and
      (JsPath \ "criterionText").format[String] and
      (JsPath \ "criterionValue").formatNullable[String](Format(Reads.of[String].filter(ValidationError("unknown value"))({ x =>
        x == "Yes" || x == "No"
      }), Writes.of[String])) and
      (JsPath \ "criterionIsDefining").format[Boolean] and
      (JsPath \ "criterionIsCompound").format[Boolean] and
      (JsPath \ "arms").format[List[String]] and
      (JsPath \ "eligibilityStatus").formatNullable[String](Format(Reads.of[String].filter(ValidationError("unknown status"))({
        case x if LabelValue.fromString(x).isDefined => true
        case _                                       => false
      }), Writes.of[String])) and
      (JsPath \ "verifiedEligibilityStatus").formatNullable[String](Format(
        Reads.of[String].filter(ValidationError("unknown status"))({
          case x if LabelValue.fromString(x).isDefined => true
          case _                                       => false
        }), Writes.of[String])) and
      (JsPath \ "isVerified").format[Boolean] and
      (JsPath \ "isVisible").format[Boolean] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "inclusion").formatNullable[Boolean]
    ) (ApiPatientCriterion.apply, unlift(ApiPatientCriterion.unapply))

  def fromDomain(patientCriterion: PatientCriterion,
                 labelId: LongId[Label],
                 arms: List[PatientCriterionArm]) = ApiPatientCriterion(
    id = patientCriterion.id.id,
    labelId = labelId.id,
    nctId = patientCriterion.nctId.id,
    criterionId = patientCriterion.criterionId.id,
    criterionText = patientCriterion.criterionText,
    criterionValue = patientCriterion.criterionValue.map { x =>
      LabelValue.fromBoolean(x).toString
    },
    criterionIsDefining = patientCriterion.criterionIsDefining,
    criterionIsCompound = patientCriterion.criterionValue.isEmpty,
    arms = arms.map(_.armName),
    eligibilityStatus = patientCriterion.eligibilityStatus.map(_.toString),
    verifiedEligibilityStatus = patientCriterion.verifiedEligibilityStatus.map(_.toString),
    isVerified = patientCriterion.isVerified,
    isVisible = patientCriterion.isVisible,
    lastUpdate = ZonedDateTime.of(patientCriterion.lastUpdate, ZoneId.of("Z")),
    inclusion = patientCriterion.inclusion
  )
}
