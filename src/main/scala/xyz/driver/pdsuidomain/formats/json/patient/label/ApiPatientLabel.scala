package xyz.driver.pdsuidomain.formats.json.patient.label

import xyz.driver.pdsuidomain.entities.PatientLabel
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.entities.labels.LabelValue

final case class ApiPatientLabel(id: Long,
                                 labelId: Long,
                                 primaryValue: Option[String],
                                 verifiedPrimaryValue: Option[String],
                                 score: Int,
                                 isImplicitMatch: Boolean,
                                 isVisible: Boolean,
                                 isVerified: Boolean)

object ApiPatientLabel {

  implicit val apiPatientLabelJsonFormat: Format[ApiPatientLabel] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "labelId").format[Long] and
      (JsPath \ "primaryValue").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown value"))({
                   case x if LabelValue.fromString(x).isDefined => true
                   case _                                       => false
                 }),
               Writes.of[String])) and
      (JsPath \ "verifiedPrimaryValue").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown value"))({
                   case x if LabelValue.fromString(x).isDefined => true
                   case _                                       => false
                 }),
               Writes.of[String])) and
      (JsPath \ "score").format[Int] and
      (JsPath \ "isImplicitMatch").format[Boolean] and
      (JsPath \ "isVisible").format[Boolean] and
      (JsPath \ "isVerified").format[Boolean]
  )(ApiPatientLabel.apply, unlift(ApiPatientLabel.unapply))

  def fromDomain(patientLabel: PatientLabel, isVerified: Boolean): ApiPatientLabel = ApiPatientLabel(
    id = patientLabel.id.id,
    labelId = patientLabel.labelId.id,
    primaryValue = patientLabel.primaryValue.map(_.toString),
    verifiedPrimaryValue = patientLabel.verifiedPrimaryValue.map(_.toString),
    score = patientLabel.score,
    isImplicitMatch = patientLabel.isImplicitMatch,
    isVisible = patientLabel.isVisible,
    isVerified = isVerified
  )
}
