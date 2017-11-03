package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.DocumentIssue

class DocumentIssueFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.documentissue._

  "Json format for DocumentIssue" should "read and write correct JSON" in {
    val documentIssue = DocumentIssue(
      id = LongId(10),
      documentId = LongId(1),
      userId = xyz.driver.core.Id("userId-001"),
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      isDraft = false,
      text = "message text",
      archiveRequired = false,
      startPage = Some(1.0),
      endPage = Some(2.0)
    )
    val writtenJson = documentIssueFormat.write(documentIssue)

    writtenJson should be(
      """{"id":10,"userId":"userId-001","documentId":1,"lastUpdate":"2017-08-10T18:00Z","isDraft":false,
        "text":"message text","archiveRequired":false,"startPage":1.0,"endPage":2.0}""".parseJson)

    val createDocumentIssueJson = """{"text":"message text","startPage":1.0,"endPage":2.0}""".parseJson
    val expectedCreatedDocumentIssue =
      documentIssue.copy(id = LongId(0), lastUpdate = LocalDateTime.MIN, isDraft = true)
    val parsedCreateDocumentIssue = jsValueToDocumentIssue(createDocumentIssueJson, LongId(1), xyz.driver.core.Id("userId-001"))
    parsedCreateDocumentIssue should be(expectedCreatedDocumentIssue)

    val updateDocumentIssueJson =
      """{"text":"new issue text","evidence":"issue evidence","archiveRequired":true,"startPage":1.0,"endPage":4.0}""".parseJson
    val expectedUpdatedDocumentIssue = documentIssue.copy(
      text = "new issue text",
      archiveRequired = true,
      endPage = Some(4.0)
    )
    val parsedUpdateDocumentIssue = applyUpdateToDocumentIssue(updateDocumentIssueJson, documentIssue)
    parsedUpdateDocumentIssue should be(expectedUpdatedDocumentIssue)
  }

}
