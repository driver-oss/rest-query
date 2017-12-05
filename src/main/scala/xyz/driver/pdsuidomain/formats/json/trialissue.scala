package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json.{RootJsonReader, _}
import xyz.driver.core.{Id, auth}
import xyz.driver.core.auth.User
import xyz.driver.core.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities._

object trialissue {

  import DefaultJsonProtocol._
  import common._

  private def deserializationErrorFieldMessage(field: String, json: JsValue)(implicit className: String) = {
    deserializationError(s"$className json object do not contain '$field' field: $json")
  }

  private def deserializationErrorEntityMessage(json: JsValue)(implicit className: String) = {
    deserializationError(s"Expected Json Object as $className, but got $json")
  }

  def applyUpdateToTrialIssue(json: JsValue, orig: TrialIssue): TrialIssue = {
    json.asJsObject.getFields("text", "evidence", "archiveRequired", "meta") match {
      case Seq(text, evidence, archiveRequired, meta) =>
        orig.copy(
          text = text.convertTo[String],
          evidence = evidence.convertTo[String],
          archiveRequired = archiveRequired.convertTo[Boolean],
          meta = meta.convertTo[String]
        )

      case _ => deserializationError(s"Expected Json Object as partial TrialIssue, but got $json")
    }
  }

  def jsValueToTrialIssue(json: JsValue, trialId: StringId[Trial], userId: xyz.driver.core.Id[User]): TrialIssue = {
    json.asJsObject.getFields("text", "evidence", "meta") match {
      case Seq(text, evidence, meta) =>
        TrialIssue(
          id = json.asJsObject.fields.get("id").flatMap(_.convertTo[Option[LongId[TrialIssue]]]).getOrElse(LongId(0)),
          userId = userId,
          trialId = trialId,
          lastUpdate = LocalDateTime.MIN,
          isDraft = true,
          text = text.convertTo[String],
          evidence = evidence.convertTo[String],
          archiveRequired = false,
          meta = meta.convertTo[String]
        )

      case _ => deserializationError(s"Expected Json Object as TrialIssue, but got $json")
    }

  }

  implicit val trialIssueWriter = new RootJsonWriter[TrialIssue] {
    override def write(obj: TrialIssue) = JsObject(
      "id" -> obj.id.toJson,
      "text" -> obj.text.toJson,
      "lastUpdate" -> obj.lastUpdate.toJson,
      "userId" -> obj.userId.toJson,
      "isDraft" -> obj.isDraft.toJson,
      "evidence" -> obj.evidence.toJson,
      "archiveRequired" -> obj.archiveRequired.toJson,
      "meta" -> obj.meta.toJson
    )
  }

  def trialIssueReader(trialId: StringId[Trial]): RootJsonReader[TrialIssue] =
    new RootJsonReader[TrialIssue] {
      implicit val className: String = "TrialIssue"

      override def read(json: JsValue): TrialIssue = json match {
        case JsObject(fields) =>
          val id = fields
            .get("id")
            .map(_.convertTo[LongId[TrialIssue]])
            .getOrElse(deserializationErrorFieldMessage("id", json))

          val text = fields
            .get("text")
            .map(_.convertTo[String])
            .getOrElse(deserializationErrorFieldMessage("text", json))

          val lastUpdate = fields
            .get("lastUpdate")
            .map(_.convertTo[LocalDateTime])
            .getOrElse(deserializationErrorFieldMessage("lastUpdate", json))

          val userId = fields
            .get("userId")
            .map(_.convertTo[Id[auth.User]])
            .getOrElse(deserializationErrorFieldMessage("userId", json))

          val isDraft = fields
            .get("isDraft")
            .map(_.convertTo[Boolean])
            .getOrElse(deserializationErrorFieldMessage("isDraft", json))

          val evidence = fields
            .get("evidence")
            .map(_.convertTo[String])
            .getOrElse(deserializationErrorFieldMessage("evidence", json))

          val archiveRequired = fields
            .get("archiveRequired")
            .map(_.convertTo[Boolean])
            .getOrElse(deserializationErrorFieldMessage("archiveRequired", json))

          val meta = fields
            .get("meta")
            .map(_.convertTo[String])
            .getOrElse(deserializationErrorFieldMessage("meta", json))

          TrialIssue(
            id = id,
            userId = userId,
            trialId = trialId,
            lastUpdate = lastUpdate,
            isDraft = isDraft,
            text = text,
            evidence = evidence,
            archiveRequired = archiveRequired,
            meta = meta
          )

        case _ => deserializationErrorEntityMessage(json)
      }
    }

}
