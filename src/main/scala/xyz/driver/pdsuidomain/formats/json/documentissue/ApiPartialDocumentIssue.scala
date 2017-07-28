package xyz.driver.pdsuidomain.formats.json.documentissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities.{Document, DocumentIssue}

final case class ApiPartialDocumentIssue(startPage: Option[Double],
                                         endPage: Option[Double],
                                         text: String,
                                         evidence: String,
                                         archiveRequired: Boolean,
                                         meta: String) {
  def applyTo(x: DocumentIssue): DocumentIssue = x.copy(
    startPage = startPage,
    endPage = endPage,
    text = text,
    evidence = evidence,
    archiveRequired = archiveRequired,
    meta = meta
  )

  def toDomain(userId: StringId[User], documentId: LongId[Document]) =
    DocumentIssue(
      id = LongId(0),
      userId = userId,
      documentId = documentId,
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

object ApiPartialDocumentIssue {
  implicit val format: Format[ApiPartialDocumentIssue] = (
    (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "text").format[String] and
      (JsPath \ "evidence").format[String] and
      (JsPath \ "archiveRequired").format[Boolean] and
      (JsPath \ "meta").format[String](Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiPartialDocumentIssue.apply, unlift(ApiPartialDocumentIssue.unapply))
}
