package xyz.driver.pdsuidomain.formats.json.trialhistory

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.TrialHistory

final case class ApiTrialHistory(id: Long,
                                 executor: String,
                                 trialId: String,
                                 state: String,
                                 action: String,
                                 created: ZonedDateTime)

object ApiTrialHistory {
  implicit val format: Format[ApiTrialHistory] = Json.format[ApiTrialHistory]

  def fromDomain(x: TrialHistory) = ApiTrialHistory(
    id = x.id.id,
    executor = x.executor.id,
    trialId = x.trialId.id,
    state = TrialHistory.State.stateToString(x.state),
    action = TrialHistory.Action.actionToString(x.action),
    created = ZonedDateTime.of(x.created, ZoneId.of("Z"))
  )
}
