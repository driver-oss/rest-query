package xyz.driver.pdsuidomain.formats.json.linkedpatient

import java.util.UUID

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.services.LinkedPatientService.RichLinkedPatient

final case class ApiLinkedPatient(email: String, name: String, patientId: UUID, trialId: String) {

  def toDomain = RichLinkedPatient(
    email = Email(email),
    name = name,
    patientId = UuidId(patientId),
    trialId = StringId(trialId)
  )
}

object ApiLinkedPatient {

  implicit val format: Format[ApiLinkedPatient] = Json.format[ApiLinkedPatient]

  def fromDomain(entity: RichLinkedPatient) = ApiLinkedPatient(
    email = entity.email.value,
    name = entity.name,
    patientId = entity.patientId.id,
    trialId = entity.trialId.id
  )
}
