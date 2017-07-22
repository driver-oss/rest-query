package xyz.driver.pdsuidomain.formats.json.trial

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}

import xyz.driver.pdsuidomain.entities.Trial
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiTrial(id: String,
                          externalId: UUID,
                          lastUpdate: ZonedDateTime,
                          status: String,
                          assignee: Option[String],
                          previousStatus: Option[String],
                          previousAssignee: Option[String],
                          lastActiveUser: Option[String],
                          condition: String,
                          phase: String,
                          hypothesisId: Option[UUID],
                          studyDesignId: Option[Long],
                          originalStudyDesign: Option[String],
                          isPartner: Boolean,
                          overview: Option[String],
                          overviewTemplate: String,
                          isUpdated: Boolean,
                          title: String,
                          originalTitle: String) {

  def toDomain = Trial(
    id = StringId(this.id),
    externalId = UuidId(this.externalId),
    status = TrialStatus.statusFromString(this.status),
    assignee = this.assignee.map(id => StringId(id)),
    previousStatus = this.previousStatus.map(s => TrialStatus.statusFromString(s)),
    previousAssignee = this.previousAssignee.map(id => StringId(id)),
    lastActiveUserId = this.lastActiveUser.map(id => StringId(id)),
    lastUpdate = this.lastUpdate.toLocalDateTime,
    condition = Trial.Condition
      .fromString(this.condition)
      .getOrElse(
        throw new NoSuchElementException(s"unknown condition ${this.condition}")
      ),
    phase = this.phase,
    hypothesisId = this.hypothesisId.map(id => UuidId(id)),
    studyDesignId = this.studyDesignId.map(id => LongId(id)),
    originalStudyDesign = this.originalStudyDesign,
    isPartner = this.isPartner,
    overview = this.overview,
    overviewTemplate = this.overviewTemplate,
    isUpdated = this.isUpdated,
    title = this.title,
    originalTitle = this.originalTitle
  )

}

object ApiTrial {

  implicit val format: Format[ApiTrial] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "externalid").format[UUID] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "status").format[String] and
      (JsPath \ "assignee").formatNullable[String] and
      (JsPath \ "previousStatus").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[String] and
      (JsPath \ "lastActiveUser").formatNullable[String] and
      (JsPath \ "condition").format[String] and
      (JsPath \ "phase").format[String] and
      (JsPath \ "hypothesisId").formatNullable[UUID] and
      (JsPath \ "studyDesignId").formatNullable[Long] and
      (JsPath \ "originalStudyDesignId").formatNullable[String] and
      (JsPath \ "isPartner").format[Boolean] and
      (JsPath \ "overview").formatNullable[String] and
      (JsPath \ "overviewTemplate").format[String] and
      (JsPath \ "isUpdated").format[Boolean] and
      (JsPath \ "title").format[String] and
      (JsPath \ "otiginalTitle").format[String]
  )(ApiTrial.apply, unlift(ApiTrial.unapply))

  def fromDomain(trial: Trial): ApiTrial = ApiTrial(
    id = trial.id.id,
    externalId = trial.externalId.id,
    status = TrialStatus.statusToString(trial.status),
    assignee = trial.assignee.map(_.id),
    previousStatus = trial.previousStatus.map(TrialStatus.statusToString),
    previousAssignee = trial.previousAssignee.map(_.id),
    lastActiveUser = trial.lastActiveUserId.map(_.id),
    lastUpdate = ZonedDateTime.of(trial.lastUpdate, ZoneId.of("Z")),
    condition = trial.condition.toString,
    phase = trial.phase,
    hypothesisId = trial.hypothesisId.map(_.id),
    studyDesignId = trial.studyDesignId.map(_.id),
    originalStudyDesign = trial.originalStudyDesign,
    isPartner = trial.isPartner,
    overview = trial.overview,
    overviewTemplate = trial.overviewTemplate,
    isUpdated = trial.isUpdated,
    title = trial.title,
    originalTitle = trial.originalTitle
  )
}
