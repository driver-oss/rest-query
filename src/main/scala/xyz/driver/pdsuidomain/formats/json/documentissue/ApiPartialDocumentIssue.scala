package xyz.driver.pdsuidomain.formats.json.documentissue

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities.{Document, DocumentIssue}

final case class ApiPartialDocumentIssue(startPage: Option[Double],
                                         endPage: Option[Double],
                                         text: String,
                                         archiveRequired: Boolean) {
  def applyTo(x: DocumentIssue): DocumentIssue = x.copy(
    startPage = startPage,
    endPage = endPage,
    text = text,
    archiveRequired = archiveRequired
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
      archiveRequired = false
    )
}

object ApiPartialDocumentIssue {
  implicit val format: Format[ApiPartialDocumentIssue] = (
    (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "text").format[String] and
      (JsPath \ "archiveRequired").format[Boolean]
  )(ApiPartialDocumentIssue.apply, unlift(ApiPartialDocumentIssue.unapply))
}
