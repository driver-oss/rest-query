package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.MedicalRecordIssue

class MedicalRecordIssueFormatSuite extends FlatSpec with Matchers {
  import recordissue._

  "Json format for MedicalRecordIssue" should "read and write correct JSON" in {
    val recordIssue = MedicalRecordIssue(
      id = LongId(10),
      recordId = LongId(1),
      userId = StringId("userId-001"),
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      isDraft = false,
      text = "message text",
      archiveRequired = false,
      startPage = Some(1.0),
      endPage = Some(2.0)
    )
    val writtenJson = recordIssueWriter.write(recordIssue)

    writtenJson should be(
      """{"id":10,"userId":"userId-001","lastUpdate":"2017-08-10T18:00Z","isDraft":false,
        "text":"message text","archiveRequired":false,"startPage":1.0,"endPage":2.0}""".parseJson)

    val createRecordIssueJson = """{"text":"message text","startPage":1.0,"endPage":2.0}""".parseJson
    val expectedCreatedRecordIssue = recordIssue.copy(id = LongId(0), lastUpdate = LocalDateTime.MIN, isDraft = true)
    val parsedCreateRecordIssue = jsValueToRecordIssue(createRecordIssueJson, LongId(1), StringId("userId-001"))
    parsedCreateRecordIssue should be(expectedCreatedRecordIssue)

    val updateRecordIssueJson =
      """{"text":"new issue text","evidence":"issue evidence","archiveRequired":true,"startPage":1.0,"endPage":4.0}""".parseJson
    val expectedUpdatedRecordIssue = recordIssue.copy(
      text = "new issue text",
      archiveRequired = true,
      endPage = Some(4.0)
    )
    val parsedUpdateRecordIssue = applyUpdateToRecordIssue(updateRecordIssueJson, recordIssue)
    parsedUpdateRecordIssue should be(expectedUpdatedRecordIssue)
  }

}
