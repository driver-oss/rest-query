package xyz.driver.pdsuidomain.formats.json.patient.hypothesis

import xyz.driver.pdsuidomain.entities.PatientHypothesis
import org.davidbild.tristate.Tristate
import org.davidbild.tristate.contrib.play.ToJsPathOpsFromJsPath
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiPartialPatientHypothesis(rationale: Tristate[String]) {

  def applyTo(orig: PatientHypothesis): PatientHypothesis = {
    orig.copy(
      rationale = rationale.cata(Some(_), None, orig.rationale)
    )
  }
}

object ApiPartialPatientHypothesis {

  implicit val reads: Reads[ApiPartialPatientHypothesis] =
    (__ \ "rationale").readTristate[String].map(x => ApiPartialPatientHypothesis(x))

  implicit val writes: Writes[ApiPartialPatientHypothesis] =
    (__ \ "rationale").writeTristate[String].contramap(_.rationale)

  implicit val format: Format[ApiPartialPatientHypothesis] = Format(reads, writes)
}
