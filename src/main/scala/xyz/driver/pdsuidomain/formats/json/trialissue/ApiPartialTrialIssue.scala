package xyz.driver.pdsuidomain.formats.json.trialissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities.{Trial, TrialIssue}

final case class ApiPartialTrialIssue(text: String, evidence: String, archiveRequired: Boolean, meta: String) {
  def applyTo(x: TrialIssue): TrialIssue = x.copy(
    text = text,
    evidence = evidence,
    archiveRequired = archiveRequired,
    meta = meta
  )

  def toDomain(userId: StringId[User], trialId: StringId[Trial]) = TrialIssue(
    id = LongId(0),
    userId = userId,
    trialId = trialId,
    lastUpdate = LocalDateTime.MIN,
    isDraft = true,
    text = text,
    evidence = evidence,
    archiveRequired = false,
    meta = meta
  )
}

object ApiPartialTrialIssue {
  implicit val format: Format[ApiPartialTrialIssue] = (
    (JsPath \ "text").format[String] and
      (JsPath \ "evidence").format[String] and
      (JsPath \ "archiveRequired").format[Boolean] and
      (JsPath \ "meta").format[String](Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiPartialTrialIssue.apply, unlift(ApiPartialTrialIssue.unapply))
}
