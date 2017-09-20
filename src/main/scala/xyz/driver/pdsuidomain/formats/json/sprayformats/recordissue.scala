package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, User}
import xyz.driver.pdsuidomain.entities._

object recordissue {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToRecordIssue(json: JsValue, orig: MedicalRecordIssue): MedicalRecordIssue = json match {
    case JsObject(fields) =>
      val text = fields
        .get("text")
        .map(_.convertTo[String])
        .getOrElse(deserializationError(s"MedicalRecordIssue json object does not contain `text` field: $json"))

      val archiveRequired = fields
        .get("archiveRequired")
        .map(_.convertTo[Boolean])
        .getOrElse(
          deserializationError(s"MedicalRecordIssue json object does not contain `archiveRequired` field: $json"))

      val startPage = fields.get("startPage").map(_.convertTo[Double])
      val endPage   = fields.get("endPage").map(_.convertTo[Double])

      orig.copy(
        text = text,
        archiveRequired = archiveRequired,
        startPage = startPage,
        endPage = endPage
      )

    case _ => deserializationError(s"Expected Json Object as partial MedicalRecordIssue, but got $json")

  }

  def jsValueToRecordIssue(json: JsValue,
                           recordId: LongId[MedicalRecord],
                           userId: StringId[User]): MedicalRecordIssue = json match {
    case JsObject(fields) =>
      val text = fields
        .get("text")
        .map(_.convertTo[String])
        .getOrElse(deserializationError(s"MedicalRecordIssue json object does not contain `text` field: $json"))

      val startPage = fields.get("startPage").map(_.convertTo[Double])
      val endPage   = fields.get("endPage").map(_.convertTo[Double])
      MedicalRecordIssue(
        id = LongId(0),
        userId = userId,
        recordId = recordId,
        lastUpdate = LocalDateTime.MIN,
        isDraft = true,
        text = text,
        archiveRequired = false,
        startPage = startPage,
        endPage = endPage
      )

    case _ => deserializationError(s"Expected Json Object as MedicalRecordIssue, but got $json")
  }

  implicit val recordIssueFormat = jsonFormat9(MedicalRecordIssue.apply)

}
