package xyz.driver.pdsuidomain.formats.json.patient.hypothesis

import java.util.UUID

import xyz.driver.pdsuidomain.entities.PatientHypothesis
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiPatientHypothesis(id: UUID,
                                      patientId: String,
                                      hypothesisId: UUID,
                                      matchedTrials: Long,
                                      rationale: Option[String])

object ApiPatientHypothesis {

  implicit val apiPatientHypothesisJsonFormat: Format[ApiPatientHypothesis] = (
    (JsPath \ "id").format[UUID] and
      (JsPath \ "patientId").format[String] and
      (JsPath \ "hypothesisId").format[UUID] and
      (JsPath \ "matchedTrials").format[Long] and
      (JsPath \ "rationale").formatNullable[String]
    ) (ApiPatientHypothesis.apply, unlift(ApiPatientHypothesis.unapply))

  def fromDomain(patientHypothesis: PatientHypothesis) = ApiPatientHypothesis(
    id = patientHypothesis.id.id,
    patientId = patientHypothesis.patientId.toString,
    hypothesisId = patientHypothesis.hypothesisId.id,
    matchedTrials = patientHypothesis.matchedTrials,
    rationale = patientHypothesis.rationale
  )
}
