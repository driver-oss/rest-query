package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientHypothesisService.RichPatientHypothesis

object patienthypothesis {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientHypothesis(json: JsValue, orig: PatientHypothesis): PatientHypothesis = json match {
    case JsObject(fields) =>
      val rationale = if (fields.contains("rationale")) {
        fields.get("rationale").map(_.convertTo[String])
      } else orig.rationale

      orig.copy(rationale = rationale)

    case _ => deserializationError(s"Expected Json Object as partial PatientHypothesis, but got $json")
  }

  implicit val richPatientHypothesisWriter: RootJsonWriter[RichPatientHypothesis] =
    new RootJsonWriter[RichPatientHypothesis] {
      override def write(obj: RichPatientHypothesis): JsValue = {
        JsObject(
          "id"                  -> obj.patientHypothesis.id.toJson,
          "patientId"           -> obj.patientHypothesis.patientId.toJson,
          "hypothesisId"        -> obj.patientHypothesis.hypothesisId.toJson,
          "matchedTrials"       -> obj.patientHypothesis.matchedTrials.toJson,
          "rationale"           -> obj.patientHypothesis.rationale.toJson,
          "isRationaleRequired" -> obj.isRequired.toJson
        )
      }
    }

  implicit val patientHypothesisWriter: RootJsonWriter[PatientHypothesis] =
    new RootJsonWriter[PatientHypothesis] {
      override def write(obj: PatientHypothesis): JsValue = {
        JsObject(
          "id"                  -> obj.id.toJson,
          "patientId"           -> obj.patientId.toJson,
          "hypothesisId"        -> obj.hypothesisId.toJson,
          "matchedTrials"       -> obj.matchedTrials.toJson,
          "rationale"           -> obj.rationale.toJson
        )
      }
    }

}
