package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.PatientHistory

class PatientHistoryFormatSuite extends FlatSpec with Matchers {
  import patienthistory._

  "Json format for PatientHistory" should "read and write correct JSON" in {
    val patientHistory = PatientHistory(
      id = LongId(10),
      patientId = UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
      executor = StringId("userId-001"),
      state = PatientHistory.State.Verify,
      action = PatientHistory.Action.Start,
      created = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val writtenJson = patientHistoryFormat.write(patientHistory)

    writtenJson should be(
      """{"id":10,"executor":"userId-001","patientId":"40892a07-c638-49d2-9795-1edfefbbcc7c","state":"Verify",
        "action":"Start","created":"2017-08-10T18:00Z"}""".parseJson)

    val parsedPatientHistory = patientHistoryFormat.read(writtenJson)
    parsedPatientHistory should be(patientHistory)
  }

}
