package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.core.generators.{nextBoolean, nextInt, nextOption, nextString}
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuidomain.fakes.entities.common.nextLocalDateTime

object BridgeUploadQueueGen {
  private val maxAttemptsNumber = 100

  def nextBridgeUploadQueueItem(): BridgeUploadQueue.Item = {
    BridgeUploadQueue.Item(
      kind = nextString(),
      tag = nextString(),
      created = nextLocalDateTime,
      attempts = nextInt(maxAttemptsNumber, minValue = 0),
      nextAttempt = nextLocalDateTime,
      completed = nextBoolean(),
      dependencyKind = nextOption(nextString()),
      dependencyTag = nextOption(nextString())
    )
  }
}
