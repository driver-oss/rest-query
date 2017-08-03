package xyz.driver.pdsuidomain.formats.json.patientissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities.{Patient, PatientIssue}

final case class ApiPartialPatientIssue(text: String, archiveRequired: Boolean) {
  def applyTo(x: PatientIssue): PatientIssue = x.copy(
    text = text,
    archiveRequired = archiveRequired
  )

  def toDomain(userId: StringId[User], patientId: UuidId[Patient]) =
    PatientIssue(
      id = LongId(0),
      userId = userId,
      patientId = patientId,
      lastUpdate = LocalDateTime.MIN,
      isDraft = true,
      text = text,
      archiveRequired = false
    )
}

object ApiPartialPatientIssue {
  implicit val format: Format[ApiPartialPatientIssue] = (
    (JsPath \ "text").format[String] and
      (JsPath \ "archiveRequired").format[Boolean]
  )(ApiPartialPatientIssue.apply, unlift(ApiPartialPatientIssue.unapply))
}
