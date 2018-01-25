package xyz.driver.pdsuidomain.formats.json

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import spray.json._
import xyz.driver.core.auth.User
import xyz.driver.core.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities._

object trial {
  import DefaultJsonProtocol._
  import Trial._
  import common._

  implicit val trialStatusFormat: RootJsonFormat[Status] = new EnumJsonFormat[Status](
    "New"            -> Status.New,
    "ReviewSummary"  -> Status.ReviewSummary,
    "Summarized"     -> Status.Summarized,
    "PendingUpdate"  -> Status.PendingUpdate,
    "Update"         -> Status.Update,
    "ReviewCriteria" -> Status.ReviewCriteria,
    "Done"           -> Status.Done,
    "Flagged"        -> Status.Flagged,
    "Archived"       -> Status.Archived
  )

  implicit val trialFormat: RootJsonFormat[Trial] = new RootJsonFormat[Trial] {
    override def write(obj: Trial) =
      JsObject(
        "id"                    -> obj.id.toJson,
        "externalid"            -> obj.externalId.toJson,
        "lastUpdate"            -> ZonedDateTime.of(obj.lastUpdate, ZoneId.of("Z")).toJson,
        "status"                -> obj.status.toJson,
        "assignee"              -> obj.assignee.toJson,
        "previousStatus"        -> obj.previousStatus.toJson,
        "previousAssignee"      -> obj.previousAssignee.toJson,
        "lastActiveUser"        -> obj.lastActiveUserId.toJson,
        "phase"                 -> obj.phase.toJson,
        "hypothesisId"          -> obj.hypothesisId.toJson,
        "studyDesignId"         -> obj.studyDesignId.toJson,
        "originalStudyDesignId" -> obj.originalStudyDesign.toJson,
        "isPartner"             -> obj.isPartner.toJson,
        "overview"              -> obj.overview.toJson,
        "overviewTemplate"      -> obj.overviewTemplate.toJson,
        "isUpdated"             -> obj.isUpdated.toJson,
        "title"                 -> obj.title.toJson,
        "originalTitle"         -> obj.originalTitle.toJson
      )

    override def read(json: JsValue): Trial = {
      json match {
        case JsObject(fields) =>
          val id = fields
            .get("id")
            .map(_.convertTo[StringId[Trial]])
            .getOrElse(deserializationError(s"Trial create json object does not contain `id` field: $json"))
          val externalid = fields
            .get("externalid")
            .map(_.convertTo[UuidId[Trial]])
            .getOrElse(deserializationError(s"Trial create json object does not contain `externalid` field: $json"))
          val status = fields
            .get("status")
            .map(_.convertTo[Trial.Status])
            .getOrElse(deserializationError(s"Trial create json object does not contain `status` field: $json"))
          val assignee = fields
            .get("assignee")
            .flatMap(_.convertTo[Option[xyz.driver.core.Id[User]]])
          val previousStatus = fields
            .get("previousStatus")
            .flatMap(_.convertTo[Option[Trial.Status]])
          val previousAssignee = fields
            .get("previousAssignee")
            .flatMap(_.convertTo[Option[xyz.driver.core.Id[User]]])
          val lastActiveUser = fields
            .get("lastActiveUser")
            .flatMap(_.convertTo[Option[xyz.driver.core.Id[User]]])
          val lastUpdate = fields
            .get("lastUpdate")
            .map(_.convertTo[LocalDateTime])
            .getOrElse(deserializationError(s"Trial create json object does not contain `lastUpdate` field: $json"))
          val phase = fields
            .get("phase")
            .map(_.convertTo[String])
            .getOrElse(deserializationError(s"Trial create json object does not contain `phase` field: $json"))
          val hypothesisId = fields
            .get("hypothesisId")
            .flatMap(_.convertTo[Option[UuidId[Hypothesis]]])
          val studyDesignId = fields
            .get("studyDesignId")
            .flatMap(_.convertTo[Option[LongId[StudyDesign]]])
          val originalStudyDesignId = fields
            .get("originalStudyDesignId")
            .flatMap(_.convertTo[Option[String]])
          val isPartner = fields
            .get("isPartner")
            .map(_.convertTo[Boolean])
            .getOrElse(deserializationError(s"Trial create json object does not contain `isPartner` field: $json"))
          val overview = fields
            .get("overview")
            .flatMap(_.convertTo[Option[String]])
          val overviewTemplate = fields
            .get("overviewTemplate")
            .map(_.convertTo[String])
            .getOrElse(
              deserializationError(s"Trial create json object does not contain `overviewTemplate` field: $json"))
          val isUpdated = fields
            .get("isUpdated")
            .map(_.convertTo[Boolean])
            .getOrElse(deserializationError(s"Trial create json object does not contain `isUpdated` field: $json"))
          val title = fields
            .get("title")
            .map(_.convertTo[String])
            .getOrElse(deserializationError(s"Trial create json object does not contain `title` field: $json"))
          val originalTitle = fields
            .get("originalTitle")
            .map(_.convertTo[String])
            .getOrElse(deserializationError(s"Trial create json object does not contain `originalTitle` field: $json"))

          Trial(
            id,
            externalid,
            status,
            assignee,
            previousStatus,
            previousAssignee,
            lastActiveUser,
            lastUpdate,
            phase,
            hypothesisId,
            studyDesignId,
            originalStudyDesignId,
            isPartner,
            overview,
            overviewTemplate,
            isUpdated,
            title,
            originalTitle
          )

        case _ => deserializationError(s"Expected Json Object as Trial, but got $json")
      }
    }
  }

  def applyUpdateToTrial(json: JsValue, orig: Trial): Trial = json match {
    case JsObject(fields) =>
      val hypothesisId = fields
        .get("hypothesisId")
        .map(_.convertTo[Option[UuidId[Hypothesis]]])
        .getOrElse(orig.hypothesisId)

      val studyDesignId = fields
        .get("studyDesignId")
        .map(_.convertTo[Option[LongId[StudyDesign]]])
        .getOrElse(orig.studyDesignId)

      val overview = fields
        .get("overview")
        .map(_.convertTo[Option[String]])
        .getOrElse(orig.overview)

      val title = fields
        .get("title")
        .map(_.convertTo[Option[String]].getOrElse(""))
        .getOrElse(orig.title)

      val originalTitle = fields
        .get("originalTitle")
        .flatMap(_.convertTo[Option[String]])
        .getOrElse(orig.originalTitle)

      orig.copy(
        hypothesisId = hypothesisId,
        studyDesignId = studyDesignId,
        overview = overview,
        title = title,
        originalTitle = originalTitle
      )

    case _ => deserializationError(s"Expected Json Object as Trial, but got $json")
  }

  implicit val trialCreationRequestFormat: RootJsonFormat[TrialCreationRequest] =
    jsonFormat3(TrialCreationRequest.apply)

}
