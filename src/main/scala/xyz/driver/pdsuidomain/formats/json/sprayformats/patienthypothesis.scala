package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._

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

  implicit val patientHypothesisWriter: JsonWriter[(PatientHypothesis, Boolean)] =
    new JsonWriter[(PatientHypothesis, Boolean)] {
      override def write(obj: (PatientHypothesis, Boolean)): JsValue = {
        val patientHypothesis   = obj._1
        val isRationaleRequired = obj._2
        JsObject(
          "id"                  -> patientHypothesis.id.toJson,
          "patientId"           -> patientHypothesis.patientId.toJson,
          "hypothesisId"        -> patientHypothesis.hypothesisId.toJson,
          "matchedTrials"       -> patientHypothesis.matchedTrials.toJson,
          "rationale"           -> patientHypothesis.rationale.toJson,
          "isRationaleRequired" -> isRationaleRequired.toJson
        )
      }
    }

}
