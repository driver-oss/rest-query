package xyz.driver.pdsuidomain.formats.json

import java.time.{LocalDate, LocalDateTime}

import spray.json._
import xyz.driver.core.auth.User
import xyz.driver.core.json._
import xyz.driver.entities.clinic.TestOrder
import xyz.driver.entities.common.FullName
import xyz.driver.entities.patient.CancerType
import xyz.driver.formats.json.common._
import xyz.driver.formats.json.patient._
import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuidomain.entities._

object patient {
  import DefaultJsonProtocol._
  import Patient._
  import common._

  implicit val patientStatusFormat: RootJsonFormat[Status] = new EnumJsonFormat[Status](
    "New"      -> Status.New,
    "Verified" -> Status.Verified,
    "Reviewed" -> Status.Reviewed,
    "Curated"  -> Status.Curated,
    "Done"     -> Status.Done,
    "Flagged"  -> Status.Flagged
  )

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

    override def read(json: JsValue): Patient = {
      json match {
        case JsObject(fields) =>
          val id = fields
            .get("id")
            .map(_.convertTo[UuidId[Patient]])
            .getOrElse(deserializationError(s"Patient create json object does not contain `id` field: $json"))

          val status = fields
            .get("status")
            .map(_.convertTo[Patient.Status])
            .getOrElse(deserializationError(s"Patient create json object does not contain `status` field: $json"))

          val name = fields
            .get("name")
            .map(_.convertTo[FullName[Patient]])
            .getOrElse(deserializationError(s"Patient create json object does not contain `name` field: $json"))

          val dob = fields
            .get("dob")
            .map(_.convertTo[LocalDate])
            .getOrElse(deserializationError(s"Patient create json object does not contain `dob` field: $json"))

          val assignee = fields
            .get("assignee")
            .flatMap(_.convertTo[Option[StringId[User]]])

          val previousStatus = fields
            .get("previousStatus")
            .flatMap(_.convertTo[Option[Patient.Status]])

          val previousAssignee = fields
            .get("previousAssignee")
            .flatMap(_.convertTo[Option[StringId[User]]])

          val lastActiveUser = fields
            .get("lastActiveUser")
            .flatMap(_.convertTo[Option[StringId[User]]])

          val disease = fields
            .get("disease")
            .map(_.convertTo[CancerType])
            .getOrElse(deserializationError(s"Patient create json object does not contain `disease` field: $json"))

          val orderId = fields
            .get("orderId")
            .map(_.convertTo[xyz.driver.core.Id[TestOrder]])
            .getOrElse(deserializationError(s"Patient create json object does not contain `orderId` field: $json"))

          val lastUpdate = fields
            .get("lastUpdate")
            .map(_.convertTo[LocalDateTime])
            .getOrElse(deserializationError(s"Patient create json object does not contain `lastUpdate` field: $json"))

          Patient(id,
                  status,
                  name,
                  dob,
                  assignee,
                  previousStatus,
                  previousAssignee,
                  lastActiveUser,
                  isUpdateRequired = false,
                  disease,
                  orderId,
                  lastUpdate)

        case _ => deserializationError(s"Expected Json Object as Trial, but got $json")
      }
    }
  }

}
