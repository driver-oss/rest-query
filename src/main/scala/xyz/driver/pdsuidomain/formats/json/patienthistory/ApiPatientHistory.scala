package xyz.driver.pdsuidomain.formats.json.patienthistory

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.PatientHistory

final case class ApiPatientHistory(id: Long,
                                   executor: String,
                                   patientId: UUID,
                                   state: String,
                                   action: String,
                                   created: ZonedDateTime)

object ApiPatientHistory {
  implicit val format: Format[ApiPatientHistory] =
    Json.format[ApiPatientHistory]

  def fromDomain(x: PatientHistory) = ApiPatientHistory(
    id = x.id.id,
    executor = x.executor.id,
    patientId = x.patientId.id,
    state = PatientHistory.State.stateToString(x.state),
    action = PatientHistory.Action.actionToString(x.action),
    created = ZonedDateTime.of(x.created, ZoneId.of("Z"))
  )
}
