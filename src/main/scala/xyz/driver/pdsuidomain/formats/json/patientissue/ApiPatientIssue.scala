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
                                 evidence: String,
                                 archiveRequired: Boolean,
                                 meta: String)

object ApiPatientIssue {
  implicit val format: Format[ApiPatientIssue] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "userId").format[String] and
      (JsPath \ "isDraft").format[Boolean] and
      (JsPath \ "evidence").format[String] and
      (JsPath \ "archiveRequired").format[Boolean] and
      (JsPath \ "meta").format[String](Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiPatientIssue.apply, unlift(ApiPatientIssue.unapply))

  def fromDomain(x: PatientIssue) = ApiPatientIssue(
    id = x.id.id,
    text = x.text,
    lastUpdate = ZonedDateTime.of(x.lastUpdate, ZoneId.of("Z")),
    userId = x.userId.id,
    isDraft = x.isDraft,
    evidence = x.evidence,
    archiveRequired = x.archiveRequired,
    meta = x.meta
  )
}
