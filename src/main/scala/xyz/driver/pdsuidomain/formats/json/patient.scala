package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.formats.json.common._
import xyz.driver.formats.json.patient._
import xyz.driver.pdsuidomain.entities._

object patient {
  import DefaultJsonProtocol._
  import Patient._
  import common._

  implicit val patientStatusFormat = new EnumJsonFormat[Status](
    "New"      -> Status.New,
    "Verified" -> Status.Verified,
    "Reviewed" -> Status.Reviewed,
    "Curated"  -> Status.Curated,
    "Done"     -> Status.Done,
    "Flagged"  -> Status.Flagged
  )

  implicit val patientOrderIdFormat = new RootJsonFormat[PatientOrderId] {
    override def write(orderId: PatientOrderId): JsString = JsString(orderId.toString)
    override def read(json: JsValue): PatientOrderId = json match {
      case JsString(value) => PatientOrderId(value)
      case _               => deserializationError(s"Expected string as PatientOrderId, but got $json")
    }
  }

  implicit val patientFormat: RootJsonFormat[Patient] = new RootJsonFormat[Patient] {
    override def write(patient: Patient): JsValue =
      JsObject(
        "id"               -> patient.id.toJson,
        "status"           -> patient.status.toJson,
        "name"             -> patient.name.toJson,
        "dob"              -> patient.dob.toJson,
        "assignee"         -> patient.assignee.toJson,
        "previousStatus"   -> patient.previousStatus.toJson,
        "previousAssignee" -> patient.previousAssignee.toJson,
        "lastActiveUser"   -> patient.lastActiveUserId.toJson,
        "lastUpdate"       -> patient.lastUpdate.toJson,
        "disease"          -> patient.disease.toJson,
        "orderId"          -> patient.orderId.toJson
      )

    override def read(json: JsValue): Patient = jsonReader[Patient].read(json)
  }

}