package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.core.generators.{nextBoolean, nextInt, nextOption, nextString}
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuidomain.entities.ProviderType
import xyz.driver.pdsuidomain.fakes.entities.common.{nextLocalDateTime, nextLongId}

object BridgeUploadQueueGen {
  private val maxAttemtsNumber = 100

  def nextBridgeUploadQueueItem(): BridgeUploadQueue.Item = {
    BridgeUploadQueue.Item(
      kind = nextString(),
      tag = nextString(),
      created = nextLocalDateTime,
      attempts = nextInt(maxAttemtsNumber, minValue = 0),
      nextAttempt = nextLocalDateTime,
      completed = nextBoolean(),
      dependencyKind = nextOption(nextString()),
      dependencyTag = nextOption(nextString())
    )
  }

  def nextProviderType(): ProviderType = {
    ProviderType(
      id = nextLongId[ProviderType],
      name = nextString()
    )
  }

}
