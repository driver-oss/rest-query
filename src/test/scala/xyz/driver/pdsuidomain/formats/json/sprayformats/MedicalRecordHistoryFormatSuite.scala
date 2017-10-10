package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.MedicalRecordHistory

class MedicalRecordHistoryFormatSuite extends FlatSpec with Matchers {
  import recordhistory._

  "Json format for MedicalRecordHistory" should "read and write correct JSON" in {
    val recordHistory = MedicalRecordHistory(
      id = LongId(10),
      recordId = LongId(1),
      executor = StringId("userId-001"),
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
