package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue

class BridgeUploadQueueFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.bridgeuploadqueue._

  "Json format for BridgeUploadQueue.Item" should "read and write correct JSON" in {
    val item = BridgeUploadQueue.Item(
      kind = "kind",
      tag = "tag",
      created = LocalDateTime.parse("2017-08-10T18:00:00"),
      attempts = 0,
      nextAttempt = LocalDateTime.parse("2017-08-10T18:10:00"),
      completed = false,
      dependencyKind = Some("dependency king"),
      dependencyTag = None
    )
    val writtenJson = queueUploadItemFormat.write(item)

    writtenJson should be(
      """{"kind":"kind","tag":"tag","created":"2017-08-10T18:00Z","attempts":0,"nextAttempt":"2017-08-10T18:10Z","completed":false}""".parseJson)

    val parsedItem = queueUploadItemFormat.read(writtenJson)
    parsedItem should be(item.copy(dependencyKind = None, completed = true))
  }

}
