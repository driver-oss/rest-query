package xyz.driver.pdsuidomain.formats.json.patient.label

import xyz.driver.pdsuidomain.entities.PatientLabel
import org.davidbild.tristate.Tristate
import org.davidbild.tristate.contrib.play.ToJsPathOpsFromJsPath
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.entities.labels.LabelValue

final case class ApiPartialPatientLabel(primaryValue: Option[String], verifiedPrimaryValue: Tristate[String]) {

  def applyTo(orig: PatientLabel): PatientLabel = {
    orig.copy(
      primaryValue = primaryValue.flatMap(LabelValue.fromString).orElse(orig.primaryValue),
      verifiedPrimaryValue = verifiedPrimaryValue.cata(x => LabelValue.fromString(x), None, orig.verifiedPrimaryValue)
    )
  }
}

object ApiPartialPatientLabel {

  implicit val format: Format[ApiPartialPatientLabel] = (
    (JsPath \ "primaryValue").formatNullable[String](
      Format(Reads
               .of[String]
               .filter(ValidationError("unknown primary value"))({
                 case x if LabelValue.fromString(x).isDefined => true
                 case _                                       => false
               }),
             Writes.of[String])) and
      (JsPath \ "verifiedPrimaryValue").formatTristate[String](
        Format(
          Reads
            .of[String]
            .filter(ValidationError("unknown verified primary value"))({
              case x if LabelValue.fromString(x).isDefined => true
              case _                                       => false
            }),
          Writes.of[String]
        ))
  )(ApiPartialPatientLabel.apply, unlift(ApiPartialPatientLabel.unapply))
}
