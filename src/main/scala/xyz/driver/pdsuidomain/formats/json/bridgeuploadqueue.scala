package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item

object bridgeuploadqueue {
  import DefaultJsonProtocol._
  import common._

  implicit val queueUploadItemFormat: RootJsonFormat[BridgeUploadQueue.Item] = new RootJsonFormat[Item] {
    override def write(obj: Item) =
      JsObject(
        "kind"        -> obj.kind.toJson,
        "tag"         -> obj.tag.toJson,
        "created"     -> obj.created.toJson,
        "attempts"    -> obj.attempts.toJson,
        "nextAttempt" -> obj.nextAttempt.toJson,
        "completed"   -> obj.completed.toJson
      )

    override def read(json: JsValue): Item = json match {
      case JsObject(fields) =>
        val kind = fields
          .get("kind")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"BridgeUploadQueue.Item json object does not contain `kind` field: $json"))

        val tag = fields
          .get("tag")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"BridgeUploadQueue.Item json object does not contain `tag` field: $json"))

        val created = fields
          .get("created")
          .map(_.convertTo[LocalDateTime])
          .getOrElse(
            deserializationError(s"BridgeUploadQueue.Item json object does not contain `created` field: $json"))

        val attempts = fields
          .get("attempts")
          .map(_.convertTo[Int])
          .getOrElse(
            deserializationError(s"BridgeUploadQueue.Item json object does not contain `attempts` field: $json"))

        val nextAttempt = fields
          .get("nextAttempt")
          .map(_.convertTo[LocalDateTime])
          .getOrElse(
            deserializationError(s"BridgeUploadQueue.Item json object does not contain `nextAttempt` field: $json"))

        BridgeUploadQueue.Item(
          kind = kind,
          tag = tag,
          created = created,
          attempts = attempts,
          nextAttempt = nextAttempt,
          completed = true,
          dependencyKind = None,
          dependencyTag = None
        )

      case _ => deserializationError(s"Expected Json Object as BridgeUploadQueue.Item, but got $json")
    }
  }
}
