package xyz.driver.pdsuidomain.formats.json.patient

import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util.UUID

import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{Patient, PatientOrderId}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.entities.common.FullName
import xyz.driver.entities.patient

final case class ApiPatient(id: String,
                            status: String,
                            name: String,
                            dob: LocalDate,
                            assignee: Option[String],
                            previousStatus: Option[String],
                            previousAssignee: Option[String],
                            lastActiveUser: Option[String],
                            lastUpdate: ZonedDateTime,
                            condition: String,
                            orderId: UUID) {

  private def extractStatus(status: String): Patient.Status =
    PatientStatus.statusFromString
      .applyOrElse(status, (s: String) => throw new NoSuchElementException(s"Unknown status $s"))

  private def parseName(name: String): FullName[Patient] =
    name.split(" ") match {
      case Array()                    => throw new NoSuchElementException(s"Patient's name cannot be empty")
      case Array(first)               => FullName.fromStrings[Patient](first, "", "")
      case Array(first, last)         => FullName.fromStrings[Patient](first, "", last)
      case Array(first, middle, last) => FullName.fromStrings[Patient](first, middle, last)
      case _                          => throw new NoSuchElementException(s"Patient's name is ambiguous")
    }

  def toDomain = Patient(
    id = UuidId(this.id),
    status = extractStatus(this.status),
    name = parseName(this.name),
    dob = this.dob,
    assignee = this.assignee.map(StringId(_)),
    previousStatus = this.previousStatus.map(extractStatus),
    previousAssignee = this.previousAssignee.map(StringId(_)),
    lastActiveUserId = this.lastActiveUser.map(StringId(_)),
    isUpdateRequired = false,
    cancerType = patient.CancerType
      .fromString(this.condition)
      .getOrElse(throw new IllegalArgumentException(s"Unknown cancer type ${this.condition}")),
    orderId = PatientOrderId(this.orderId),
    lastUpdate = this.lastUpdate.toLocalDateTime
  )

}

object ApiPatient {

  implicit val format: Format[ApiPatient] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "status").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "dob").format[LocalDate] and
      (JsPath \ "assignee").formatNullable[String] and
      (JsPath \ "previousStatus").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[String] and
      (JsPath \ "lastActiveUser").formatNullable[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "condition").format[String] and
      (JsPath \ "orderId").format[UUID]
  )(ApiPatient.apply, unlift(ApiPatient.unapply))

  def fromDomain(patient: Patient) = ApiPatient(
    id = patient.id.toString,
    status = PatientStatus.statusToString(patient.status),
    name = patient.name.toString(),
    dob = patient.dob,
    assignee = patient.assignee.map(_.id),
    previousStatus = patient.previousStatus.map(PatientStatus.statusToString),
    previousAssignee = patient.previousAssignee.map(_.id),
    lastActiveUser = patient.lastActiveUserId.map(_.id),
    lastUpdate = ZonedDateTime.of(patient.lastUpdate, ZoneId.of("Z")),
    condition = patient.cancerType.toString,
    orderId = patient.orderId.id
  )
}
