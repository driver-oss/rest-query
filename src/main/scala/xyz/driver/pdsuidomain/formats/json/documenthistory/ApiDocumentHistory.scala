package xyz.driver.pdsuidomain.formats.json.documenthistory

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.DocumentHistory

final case class ApiDocumentHistory(id: Long,
                                    executor: String,
                                    documentId: Long,
                                    state: String,
                                    action: String,
                                    created: ZonedDateTime)

object ApiDocumentHistory {
  implicit val format: Format[ApiDocumentHistory] =
    Json.format[ApiDocumentHistory]

  def fromDomain(x: DocumentHistory) = ApiDocumentHistory(
    id = x.id.id,
    executor = x.executor.id,
    documentId = x.documentId.id,
    state = DocumentHistory.State.stateToString(x.state),
    action = DocumentHistory.Action.actionToString(x.action),
    created = ZonedDateTime.of(x.created, ZoneId.of("Z"))
  )
}
