package xyz.driver.pdsuidomain.formats.json.patient.label

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuidomain.entities.PatientLabel

final case class ApiPatientLabelDefiningCriteria(labelId: Long, value: Option[String])

object ApiPatientLabelDefiningCriteria {

  implicit val format: Format[ApiPatientLabelDefiningCriteria] = (
    (JsPath \ "labelId").format[Long] and
      (JsPath \ "value").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown value"))({
                   case x if LabelValue.fromString(x).isDefined => true
                   case _                                       => false
                 }),
               Writes.of[String]))
  )(ApiPatientLabelDefiningCriteria.apply, unlift(ApiPatientLabelDefiningCriteria.unapply))

  def fromDomain(x: PatientLabel) = ApiPatientLabelDefiningCriteria(
    labelId = x.labelId.id,
    value = x.verifiedPrimaryValue.map(_.toString)
  )
}
