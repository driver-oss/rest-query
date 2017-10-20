package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.DocumentHistory

class DocumentHistoryFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.documenthistory._

  "Json format for DocumentHistory" should "read and write correct JSON" in {
    val documentHistory = DocumentHistory(
      id = LongId(10),
      documentId = LongId(1),
      executor = StringId("userId-001"),
      state = DocumentHistory.State.Extract,
      action = DocumentHistory.Action.Start,
      created = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val writtenJson = documentHistoryFormat.write(documentHistory)

    writtenJson should be("""{"id":10,"executor":"userId-001","documentId":1,"state":"Extract",
        "action":"Start","created":"2017-08-10T18:00Z"}""".parseJson)

    val parsedDocumentHistory = documentHistoryFormat.read(writtenJson)
    parsedDocumentHistory should be(documentHistory)
  }

}
