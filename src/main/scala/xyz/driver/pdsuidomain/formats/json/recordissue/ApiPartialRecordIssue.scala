package xyz.driver.pdsuidomain.formats.json.recordissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities.{MedicalRecord, MedicalRecordIssue}

final case class ApiPartialRecordIssue(startPage: Option[Double],
                                       endPage: Option[Double],
                                       text: String,
                                       evidence: String,
                                       archiveRequired: Boolean,
                                       meta: String) {
  def applyTo(x: MedicalRecordIssue): MedicalRecordIssue = x.copy(
    startPage = startPage,
    endPage = endPage,
    text = text,
    evidence = evidence,
    archiveRequired = archiveRequired,
    meta = meta
  )

  def toDomain(userId: StringId[User], recordId: LongId[MedicalRecord]) =
    MedicalRecordIssue(
      id = LongId(0),
      userId = userId,
      recordId = recordId,
      startPage = startPage,
      endPage = endPage,
      lastUpdate = LocalDateTime.MIN,
      isDraft = true,
      text = text,
      evidence = evidence,
      archiveRequired = false,
      meta = meta
    )
}

object ApiPartialRecordIssue {
  implicit val format: Format[ApiPartialRecordIssue] = (
    (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "text").format[String] and
      (JsPath \ "evidence").format[String] and
      (JsPath \ "archiveRequired").format[Boolean] and
      (JsPath \ "meta").format[String](Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiPartialRecordIssue.apply, unlift(ApiPartialRecordIssue.unapply))
}
