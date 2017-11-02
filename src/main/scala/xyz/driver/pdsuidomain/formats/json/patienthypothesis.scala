package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.pdsuidomain.entities._

object patienthypothesis {
  import DefaultJsonProtocol._
  import common._
  import xyz.driver.core.json._

  def applyUpdateToPatientHypothesis(json: JsValue, orig: PatientHypothesis): PatientHypothesis = json match {
    case JsObject(fields) =>
      val rationale = if (fields.contains("rationale")) {
        fields.get("rationale").map(_.convertTo[String])
      } else orig.rationale

      orig.copy(rationale = rationale)

    case _ => deserializationError(s"Expected Json Object as partial PatientHypothesis, but got $json")
  }

  implicit val patientHypothesisWriter: RootJsonWriter[PatientHypothesis] =
    new RootJsonWriter[PatientHypothesis] {
      override def write(obj: PatientHypothesis): JsValue = {
        JsObject(
          "id"            -> obj.id.toJson,
          "patientId"     -> obj.patientId.toJson,
          "hypothesisId"  -> obj.hypothesisId.toJson,
          "matchedTrials" -> obj.matchedTrials.toJson,
          "rationale"     -> obj.rationale.toJson
        )
      }
    }

}
