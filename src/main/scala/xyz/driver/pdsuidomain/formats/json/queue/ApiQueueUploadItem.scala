package xyz.driver.pdsuidomain.formats.json.queue

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import play.api.libs.json.{Format, Json}

final case class ApiQueueUploadItem(kind: String,
                                    tag: String,
                                    created: LocalDateTime,
                                    attempts: Int,
                                    nextAttempt: LocalDateTime,
                                    completed: Boolean) {
  def toDomain = BridgeUploadQueue.Item(
    kind = kind,
    tag = tag,
    created = created,
    attempts = attempts,
    nextAttempt = nextAttempt,
    completed = true,
    dependencyKind = None,
    dependencyTag = None
  )
}

object ApiQueueUploadItem {

  def fromDomain(domain: BridgeUploadQueue.Item) = ApiQueueUploadItem(
    kind = domain.kind,
    tag = domain.tag,
    created = domain.created,
    attempts = domain.attempts,
    nextAttempt = domain.nextAttempt,
    completed = domain.completed
  )

  implicit val format: Format[ApiQueueUploadItem] = Json.format[ApiQueueUploadItem]
}
