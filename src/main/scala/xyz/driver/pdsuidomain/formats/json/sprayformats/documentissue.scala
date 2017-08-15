package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
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

      val startPage = fields.get("startPage").map(_.convertTo[Double])
      val endPage   = fields.get("endPage").map(_.convertTo[Double])

      orig.copy(
        text = text,
        archiveRequired = archiveRequired,
        startPage = startPage,
        endPage = endPage
      )

    case _ => deserializationError(s"Expected Json Object as partial DocumentIssue, but got $json")

  }

  def jsValueToDocumentIssue(json: JsValue, documentId: LongId[Document], userId: StringId[User]): DocumentIssue =
    json match {
      case JsObject(fields) =>
        val text = fields
          .get("text")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"DocumentIssue json object does not contain `text` field: $json"))

        val startPage = fields.get("startPage").map(_.convertTo[Double])
        val endPage   = fields.get("endPage").map(_.convertTo[Double])
        DocumentIssue(
          id = LongId(0),
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

  implicit val documentIssueWriter = new JsonWriter[DocumentIssue] {
    override def write(obj: DocumentIssue) = JsObject(
      "id"              -> obj.id.toJson,
      "startPage"       -> obj.startPage.toJson,
      "endPage"         -> obj.endPage.toJson,
      "text"            -> obj.text.toJson,
      "lastUpdate"      -> obj.lastUpdate.toJson,
      "userId"          -> obj.userId.toJson,
      "isDraft"         -> obj.isDraft.toJson,
      "archiveRequired" -> obj.archiveRequired.toJson
    )
  }

}
