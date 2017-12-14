package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import xyz.driver.core.auth.User
import xyz.driver.core.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities._

object documentissue {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToDocumentIssue(json: JsValue, orig: DocumentIssue): DocumentIssue = json match {
    case JsObject(fields) =>
      val text = fields
        .get("text")
        .map(_.convertTo[String])
        .getOrElse(deserializationError(s"DocumentIssue json object does not contain `text` field: $json"))

      val archiveRequired = fields
        .get("archiveRequired")
        .map(_.convertTo[Boolean])
        .getOrElse(deserializationError(s"DocumentIssue json object does not contain `archiveRequired` field: $json"))

      val startPage = fields.get("startPage").map(_.convertTo[Option[Double]])
      val endPage   = fields.get("endPage").map(_.convertTo[Option[Double]])

      orig.copy(
        text = text,
        archiveRequired = archiveRequired,
        startPage = startPage.getOrElse(orig.startPage),
        endPage = endPage.getOrElse(orig.endPage)
      )

    case _ => deserializationError(s"Expected Json Object as partial DocumentIssue, but got $json")

  }

  def jsValueToDocumentIssue(json: JsValue,
                             documentId: LongId[Document],
                             userId: xyz.driver.core.Id[User]): DocumentIssue =
    json match {
      case JsObject(fields) =>
        val text = fields
          .get("text")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"DocumentIssue json object does not contain `text` field: $json"))

        val id        = fields.get("id").map(_.convertTo[LongId[DocumentIssue]])
        val startPage = fields.get("startPage").flatMap(_.convertTo[Option[Double]])
        val endPage   = fields.get("endPage").flatMap(_.convertTo[Option[Double]])

        DocumentIssue(
          id = id.getOrElse(LongId(0)),
          userId = userId,
          documentId = documentId,
          lastUpdate = LocalDateTime.MIN,
          isDraft = true,
          text = text,
          archiveRequired = false,
          startPage = startPage,
          endPage = endPage
        )

      case _ => deserializationError(s"Expected Json Object as DocumentIssue, but got $json")
    }

  implicit val documentIssueFormat: RootJsonFormat[DocumentIssue] = jsonFormat9(DocumentIssue.apply)

}
