package xyz.driver.pdsuidomain.formats.json.trial

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import xyz.driver.pdsuidomain.entities.Trial
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiTrial(id: String,
                          lastUpdate: Option[ZonedDateTime],
                          status: String,
                          assignee: Option[Long],
                          previousStatus: Option[String],
                          previousAssignee: Option[Long],
                          condition: Option[String],
                          phase: Option[String],
                          hypothesisId: Option[UUID],
                          studyDesignId: Option[Long],
                          isPartner: Boolean,
                          overview: Option[String],
                          overviewTemplate: String,
                          isUpdated: Boolean,
                          title: String)

object ApiTrial {

  implicit val format: Format[ApiTrial] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "lastUpdate").formatNullable[ZonedDateTime] and
      (JsPath \ "status").format[String] and
      (JsPath \ "assignee").formatNullable[Long] and
      (JsPath \ "previousStatus").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[Long] and
      (JsPath \ "condition").formatNullable[String] and
      (JsPath \ "phase").formatNullable[String] and
      (JsPath \ "hypothesisId").formatNullable[UUID] and
      (JsPath \ "studyDesignId").formatNullable[Long] and
      (JsPath \ "isPartner").format[Boolean] and
      (JsPath \ "overview").formatNullable[String] and
      (JsPath \ "overviewTemplate").format[String] and
      (JsPath \ "isUpdated").format[Boolean] and
      (JsPath \ "title").format[String]
    ) (ApiTrial.apply, unlift(ApiTrial.unapply))

  def fromDomain(trial: Trial): ApiTrial = ApiTrial(
    id = trial.id.id,
    status = TrialStatus.statusToString(trial.status),
    assignee = trial.assignee.map(_.id),
    previousStatus = trial.previousStatus.map(TrialStatus.statusToString),
    previousAssignee = trial.previousAssignee.map(_.id),
    lastUpdate = Option(ZonedDateTime.of(trial.lastUpdate, ZoneId.of("Z"))),
    condition = Option(trial.condition.toString),
    phase = Option(trial.phase),
    hypothesisId = trial.hypothesisId.map(_.id),
    studyDesignId = trial.studyDesignId.map(_.id),
    isPartner = trial.isPartner,
    overview = trial.overview,
    overviewTemplate = trial.overviewTemplate,
    isUpdated = trial.isUpdated,
    title = trial.title
  )
}
