package xyz.driver.pdsuidomain.formats.json.patient

import java.time.{LocalDate, ZoneId, ZonedDateTime}

import xyz.driver.pdsuidomain.entities.Patient
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

final case class ApiPatient(id: String,
                            status: String,
                            name: String,
                            dob: LocalDate,
                            assignee: Option[Long],
                            previousStatus: Option[String],
                            previousAssignee: Option[Long],
                            lastUpdate: ZonedDateTime,
                            condition: String)

object ApiPatient {

  implicit val format: Format[ApiPatient] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "status").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "dob").format[LocalDate] and
      (JsPath \ "assignee").formatNullable[Long] and
      (JsPath \ "previousStatus").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[Long] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "condition").format[String]
  )(ApiPatient.apply, unlift(ApiPatient.unapply))

  def fromDomain(patient: Patient) = ApiPatient(
    id = patient.id.toString,
    status = PatientStatus.statusToString(patient.status),
    name = patient.name,
    dob = patient.dob,
    assignee = patient.assignee.map(_.id),
    previousStatus = patient.previousStatus.map(PatientStatus.statusToString),
    previousAssignee = patient.previousAssignee.map(_.id),
    lastUpdate = ZonedDateTime.of(patient.lastUpdate, ZoneId.of("Z")),
    condition = patient.condition
  )
}
