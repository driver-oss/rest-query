package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.TrialHistory

class TrialHistoryFormatSuite extends FlatSpec with Matchers {
  import trialhistory._

  "Json format for TrialHistory" should "read and write correct JSON" in {
    val trialHistory = TrialHistory(
      id = LongId(10),
      trialId = StringId("NCT000001"),
      executor = StringId("userId-001"),
      state = TrialHistory.State.Summarize,
      action = TrialHistory.Action.Start,
      created = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val writtenJson = trialHistoryFormat.write(trialHistory)

    writtenJson should be("""{"id":10,"executor":"userId-001","trialId":"NCT000001","state":"Summarize",
        "action":"Start","created":"2017-08-10T18:00Z"}""".parseJson)

    val parsedTrialHistory = trialHistoryFormat.read(writtenJson)
    parsedTrialHistory should be(trialHistory)
  }

}
