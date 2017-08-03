package xyz.driver.pdsuidomain.formats.json.patientissue

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuidomain.entities.PatientIssue

final case class ApiPatientIssue(id: Long,
                                 text: String,
                                 lastUpdate: ZonedDateTime,
                                 userId: String,
                                 isDraft: Boolean,
                                 archiveRequired: Boolean)

object ApiPatientIssue {
  implicit val format: Format[ApiPatientIssue] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "userId").format[String] and
      (JsPath \ "isDraft").format[Boolean] and
      (JsPath \ "archiveRequired").format[Boolean]
  )(ApiPatientIssue.apply, unlift(ApiPatientIssue.unapply))

  def fromDomain(x: PatientIssue) = ApiPatientIssue(
    id = x.id.id,
    text = x.text,
    lastUpdate = ZonedDateTime.of(x.lastUpdate, ZoneId.of("Z")),
    userId = x.userId.id,
    isDraft = x.isDraft,
    archiveRequired = x.archiveRequired
  )
}
