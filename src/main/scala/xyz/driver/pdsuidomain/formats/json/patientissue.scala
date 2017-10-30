package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User, UuidId}
import xyz.driver.pdsuidomain.entities._

object patientissue {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToPatientIssue(json: JsValue, orig: PatientIssue): PatientIssue = {
    json.asJsObject.getFields("text", "archiveRequired") match {
      case Seq(text, archiveRequired) =>
        orig.copy(
          text = text.convertTo[String],
          archiveRequired = archiveRequired.convertTo[Boolean]
        )

      case _ => deserializationError(s"Expected Json Object as partial PatientIssue, but got $json")
    }
  }

  def jsValueToPatientIssue(json: JsValue, patientId: UuidId[Patient], userId: StringId[User]): PatientIssue = {
    json.asJsObject.getFields("text") match {
      case Seq(text) =>
        PatientIssue(
          id = LongId(0),
          userId = userId,
          patientId = patientId,
          lastUpdate = LocalDateTime.MIN,
          isDraft = true,
          text = text.convertTo[String],
          archiveRequired = false
        )

      case _ => deserializationError(s"Expected Json Object as PatientIssue, but got $json")
    }

  }

  implicit val patientIssueWriter: RootJsonWriter[PatientIssue] = new RootJsonWriter[PatientIssue] {
    override def write(obj: PatientIssue) = JsObject(
      "id"              -> obj.id.toJson,
      "text"            -> obj.text.toJson,
      "lastUpdate"      -> obj.lastUpdate.toJson,
      "userId"          -> obj.userId.toJson,
      "isDraft"         -> obj.isDraft.toJson,
      "archiveRequired" -> obj.archiveRequired.toJson
    )
  }

}
