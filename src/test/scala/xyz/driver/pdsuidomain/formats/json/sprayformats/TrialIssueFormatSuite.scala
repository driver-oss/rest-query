package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.TrialIssue

class TrialIssueFormatSuite extends FlatSpec with Matchers {
  import trialissue._

  "Json format for TrialIssue" should "read and write correct JSON" in {
    val trialIssue = TrialIssue(
      id = LongId(10),
      trialId = StringId("NCT000001"),
      userId = StringId("userId-001"),
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      isDraft = false,
      text = "message text",
      evidence = "evidence",
      archiveRequired = false,
      meta = "{}"
    )
    val writtenJson = trialIssueWriter.write(trialIssue)

    writtenJson should be("""{"id":10,"userId":"userId-001","lastUpdate":"2017-08-10T18:00Z","isDraft":false,
        "text":"message text","evidence":"evidence","archiveRequired":false,"meta":"{}"}""".parseJson)

    val createTrialIssueJson      = """{"text":"message text","evidence":"evidence","meta":"{}"}""".parseJson
    val expectedCreatedTrialIssue = trialIssue.copy(id = LongId(0), lastUpdate = LocalDateTime.MIN, isDraft = true)
    val parsedCreateTrialIssue =
      jsValueToTrialIssue(createTrialIssueJson, StringId("NCT000001"), StringId("userId-001"))
    parsedCreateTrialIssue should be(expectedCreatedTrialIssue)

    val updateTrialIssueJson =
      """{"text":"new issue text","evidence":"issue evidence","archiveRequired":true,
        "meta":"{\"startPage\":1.0,\"endPage\":2.0}"}""".parseJson
    val expectedUpdatedTrialIssue = trialIssue.copy(
      text = "new issue text",
      evidence = "issue evidence",
      archiveRequired = true,
      meta = """{"startPage":1.0,"endPage":2.0}"""
    )
    val parsedUpdateTrialIssue = applyUpdateToTrialIssue(updateTrialIssueJson, trialIssue)
    parsedUpdateTrialIssue should be(expectedUpdatedTrialIssue)
  }

}
