package xyz.driver.pdsuidomain.formats.json.userhistory

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.UserHistory

final case class ApiUserHistory(id: Long,
                                executor: Long,
                                recordId: Option[Long],
                                documentId: Option[Long],
                                trialId: Option[String],
                                patientId: Option[String],
                                state: String,
                                action: String,
                                created: ZonedDateTime)

object ApiUserHistory {
  implicit val format: Format[ApiUserHistory] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "executor").format[Long] and
      (JsPath \ "recordId").formatNullable[Long] and
      (JsPath \ "documentId").formatNullable[Long] and
      (JsPath \ "trialId").formatNullable[String] and
      (JsPath \ "patientId").formatNullable[String] and
      (JsPath \ "state").format[String] and
      (JsPath \ "action").format[String] and
      (JsPath \ "created").format[ZonedDateTime]
    ) (ApiUserHistory.apply, unlift(ApiUserHistory.unapply))

  def fromDomain(x: UserHistory) = ApiUserHistory(
    id = x.id.id,
    executor = x.executor.id,
    recordId = x.recordId.map(_.id),
    documentId = x.documentId.map(_.id),
    trialId = x.trialId.map(_.id),
    patientId = x.patientId.map(_.id.toString),
    state = UserHistory.State.stateToString(x.state),
    action = UserHistory.Action.actionToString(x.action),
    created = ZonedDateTime.of(x.created, ZoneId.of("Z"))
  )
}
