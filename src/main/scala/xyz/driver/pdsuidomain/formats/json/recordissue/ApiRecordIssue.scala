package xyz.driver.pdsuidomain.formats.json.recordissue

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuidomain.entities.MedicalRecordIssue

final case class ApiRecordIssue(id: Long,
                                startPage: Option[Double],
                                endPage: Option[Double],
                                text: String,
                                lastUpdate: ZonedDateTime,
                                userId: String,
                                isDraft: Boolean,
                                archiveRequired: Boolean)

object ApiRecordIssue {
  implicit val format: Format[ApiRecordIssue] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "text").format[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "userId").format[String] and
      (JsPath \ "isDraft").format[Boolean] and
      (JsPath \ "archiveRequired").format[Boolean]
  )(ApiRecordIssue.apply, unlift(ApiRecordIssue.unapply))

  def fromDomain(x: MedicalRecordIssue) = ApiRecordIssue(
    id = x.id.id,
    startPage = x.startPage,
    endPage = x.endPage,
    text = x.text,
    lastUpdate = ZonedDateTime.of(x.lastUpdate, ZoneId.of("Z")),
    userId = x.userId.id,
    isDraft = x.isDraft,
    archiveRequired = x.archiveRequired
  )
}
