package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.PatientIssue

class PatientIssueFormatSuite extends FlatSpec with Matchers {
  import patientissue._

  "Json format for PatientIssue" should "read and write correct JSON" in {
    val patientIssue = PatientIssue(
      id = LongId(10),
      patientId = UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
      userId = StringId("userId-001"),
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      isDraft = false,
      text = "message text",
      archiveRequired = false
    )
    val writtenJson = patientIssueWriter.write(patientIssue)

    writtenJson should be("""{"id":10,"userId":"userId-001","lastUpdate":"2017-08-10T18:00Z","isDraft":false,
        "text":"message text","archiveRequired":false}""".parseJson)

    val createPatientIssueJson      = """{"text":"message text"}""".parseJson
    val expectedCreatedPatientIssue = patientIssue.copy(id = LongId(0), lastUpdate = LocalDateTime.MIN, isDraft = true)
    val parsedCreatePatientIssue = jsValueToPatientIssue(createPatientIssueJson,
                                                         UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
                                                         StringId("userId-001"))
    parsedCreatePatientIssue should be(expectedCreatedPatientIssue)

    val updatePatientIssueJson =
      """{"text":"new issue text","evidence":"issue evidence","archiveRequired":true}""".parseJson
    val expectedUpdatedPatientIssue = patientIssue.copy(
      text = "new issue text",
      archiveRequired = true
    )
    val parsedUpdatePatientIssue = applyUpdateToPatientIssue(updatePatientIssueJson, patientIssue)
    parsedUpdatePatientIssue should be(expectedUpdatedPatientIssue)
  }

}
