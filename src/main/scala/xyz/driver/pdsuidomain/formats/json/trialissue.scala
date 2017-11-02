package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities._

object trialissue {
  import DefaultJsonProtocol._
  import common._

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

  def jsValueToTrialIssue(json: JsValue, trialId: StringId[Trial], userId: StringId[User]): TrialIssue = {
    json.asJsObject.getFields("text", "evidence", "meta") match {
      case Seq(text, evidence, meta) =>
        TrialIssue(
          id = LongId(0),
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
      "id"              -> obj.id.toJson,
      "text"            -> obj.text.toJson,
      "lastUpdate"      -> obj.lastUpdate.toJson,
      "userId"          -> obj.userId.toJson,
      "isDraft"         -> obj.isDraft.toJson,
      "evidence"        -> obj.evidence.toJson,
      "archiveRequired" -> obj.archiveRequired.toJson,
      "meta"            -> obj.meta.toJson
    )
  }

}
