package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.Trial

class TrialFormatSuite extends FlatSpec with Matchers {
  import trial._

  "Json format for Trial" should "read and write correct JSON" in {
    val orig = Trial(
      id = StringId("NCT000001"),
      externalId = UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
      status = Trial.Status.New,
      assignee = None,
      previousStatus = None,
      previousAssignee = None,
      lastActiveUserId = None,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:16:19"),
      phase = "",
      hypothesisId = Some(UuidId("3b80b2e2-5372-4cf5-a342-6e4ebe10fafd")),
      studyDesignId = Some(LongId(321)),
      originalStudyDesign = None,
      isPartner = false,
      overview = None,
      overviewTemplate = "",
      isUpdated = false,
      title = "trial title",
      originalTitle = "orig trial title"
    )
    val writtenJson = trialWriter.write(orig)

    writtenJson should be (
      """{"isPartner":false,"assignee":null,"lastUpdate":"2017-08-10T18:16:19Z","previousStatus":null,
        "isUpdated":false,"overviewTemplate":"","phase":"","originalStudyDesignId":null,
        "hypothesisId":"3b80b2e2-5372-4cf5-a342-6e4ebe10fafd","originalTitle":"orig trial title",
        "studyDesignId":321,"lastActiveUser":null,"externalid":"40892a07-c638-49d2-9795-1edfefbbcc7c",
        "id":"NCT000001","status":"New","overview":null,"previousAssignee":null,"title":"trial title"}""".parseJson)

    val updateTrialJson = """{"hypothesisId":null,"overview":"new overview"}""".parseJson
    val expectedUpdatedTrial = orig.copy(hypothesisId = None, overview = Some("new overview"))
    val parsedUpdateTrial = applyUpdateToTrial(updateTrialJson, orig)
    parsedUpdateTrial should be(expectedUpdatedTrial)
  }

}
