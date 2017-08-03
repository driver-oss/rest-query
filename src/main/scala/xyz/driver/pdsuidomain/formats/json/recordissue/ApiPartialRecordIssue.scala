package xyz.driver.pdsuidomain.formats.json.recordissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities.{MedicalRecord, MedicalRecordIssue}

final case class ApiPartialRecordIssue(startPage: Option[Double],
                                       endPage: Option[Double],
                                       text: String,
                                       archiveRequired: Boolean) {
  def applyTo(x: MedicalRecordIssue): MedicalRecordIssue = x.copy(
    startPage = startPage,
    endPage = endPage,
    text = text,
    archiveRequired = archiveRequired
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
      archiveRequired = false
    )
}

object ApiPartialRecordIssue {
  implicit val format: Format[ApiPartialRecordIssue] = (
    (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "text").format[String] and
      (JsPath \ "archiveRequired").format[Boolean]
  )(ApiPartialRecordIssue.apply, unlift(ApiPartialRecordIssue.unapply))
}
