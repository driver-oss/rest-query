package xyz.driver.pdsuidomain.formats.json.trialissue

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities.TrialIssue

final case class ApiTrialIssue(id: Long,
                               trialId: String,
                               text: String,
                               lastUpdate: ZonedDateTime,
                               userId: String,
                               isDraft: Boolean,
                               evidence: String,
                               archiveRequired: Boolean,
                               meta: String) {

  def toDomain = TrialIssue(
    id = LongId(this.id),
    trialId = StringId(this.trialId),
    text = this.text,
    userId = StringId(this.userId),
    lastUpdate = this.lastUpdate.toLocalDateTime,
    isDraft = this.isDraft,
    evidence = this.evidence,
    archiveRequired = this.archiveRequired,
    meta = this.meta
  )

}

object ApiTrialIssue {
  implicit val format: Format[ApiTrialIssue] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "text").format[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "userId").format[String] and
      (JsPath \ "isDraft").format[Boolean] and
      (JsPath \ "evidence").format[String] and
      (JsPath \ "archiveRequired").format[Boolean] and
      (JsPath \ "meta").format[String](Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiTrialIssue.apply, unlift(ApiTrialIssue.unapply))

  def fromDomain(x: TrialIssue) = ApiTrialIssue(
    id = x.id.id,
    trialId = x.trialId.id,
    text = x.text,
    lastUpdate = ZonedDateTime.of(x.lastUpdate, ZoneId.of("Z")),
    userId = x.userId.id,
    isDraft = x.isDraft,
    evidence = x.evidence,
    archiveRequired = x.archiveRequired,
    meta = x.meta
  )
}
