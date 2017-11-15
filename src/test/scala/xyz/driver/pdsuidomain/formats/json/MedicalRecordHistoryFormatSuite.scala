package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import org.scalatest.{FreeSpecLike, Matchers}
import spray.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.MedicalRecordHistory

class MedicalRecordHistoryFormatSuite extends FreeSpecLike with Matchers {
  import xyz.driver.pdsuidomain.formats.json.recordhistory._


  "Can read and write MedicalRecordHistory states" - {
    val states = MedicalRecordHistory.State.All
    states.foreach { state =>s"$state" in test(state)}
  }

  "Can read and write MedicalRecordHistory actions" - {
    val actions = MedicalRecordHistory.Action.All
    actions.foreach { action =>s"$action" in test(action)}
  }

  private def test(state: MedicalRecordHistory.State) = {
    recordStateFormat.read(recordStateFormat.write(state)) shouldBe state
  }

  private def test(action: MedicalRecordHistory.Action) = {
    recordActionFormat.read(recordActionFormat.write(action)) shouldBe action
  }


  "Json format for MedicalRecordHistory should read and write correct JSON" - {
    val recordHistory = MedicalRecordHistory(
      id = LongId(10),
      recordId = LongId(1),
      executor = xyz.driver.core.Id("userId-001"),
      state = MedicalRecordHistory.State.Clean,
      action = MedicalRecordHistory.Action.Start,
      created = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val writtenJson = recordHistoryFormat.write(recordHistory)

    writtenJson should be("""{"id":10,"executor":"userId-001","recordId":1,"state":"Clean",
        "action":"Start","created":"2017-08-10T18:00Z"}""".parseJson)

    val parsedRecordHistory = recordHistoryFormat.read(writtenJson)
    parsedRecordHistory should be(recordHistory)
  }

}
