package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.{ZoneId, ZonedDateTime}

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.entities._

object trial {
  import DefaultJsonProtocol._
  import common._
  import Trial._

  implicit val trialStatusFormat = new EnumJsonFormat[Status](
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

  implicit val conditionFormat = new EnumJsonFormat[Condition](
    "Breast"   -> Condition.Breast,
    "Lung"     -> Condition.Lung,
    "Prostate" -> Condition.Prostate
  )

  implicit val trialWriter: RootJsonWriter[Trial] = new RootJsonWriter[Trial] {
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

      orig.copy(
        hypothesisId = hypothesisId,
        studyDesignId = studyDesignId,
        overview = overview,
        title = title
      )

    case _ => deserializationError(s"Expected Json Object as Trial, but got $json")
  }

}
