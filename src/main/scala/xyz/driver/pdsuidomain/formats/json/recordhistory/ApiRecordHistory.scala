package xyz.driver.pdsuidomain.formats.json.recordhistory

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.MedicalRecordHistory

final case class ApiRecordHistory(id: Long,
                                  executor: String,
                                  recordId: Long,
                                  state: String,
                                  action: String,
                                  created: ZonedDateTime)

object ApiRecordHistory {
  implicit val format: Format[ApiRecordHistory] =
    Json.format[ApiRecordHistory]

  def fromDomain(x: MedicalRecordHistory) = ApiRecordHistory(
    id = x.id.id,
    executor = x.executor.id,
    recordId = x.recordId.id,
    state = MedicalRecordHistory.State.stateToString(x.state),
    action = MedicalRecordHistory.Action.actionToString(x.action),
    created = ZonedDateTime.of(x.created, ZoneId.of("Z"))
  )
}
