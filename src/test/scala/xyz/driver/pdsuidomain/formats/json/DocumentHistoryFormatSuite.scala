package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import org.scalatest.{FreeSpecLike, Matchers}
import spray.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.DocumentHistory

class DocumentHistoryFormatSuite extends FreeSpecLike with Matchers {
  import xyz.driver.pdsuidomain.formats.json.documenthistory._

  "Can read and write DocumentHistory states" - {
    val states = DocumentHistory.State.All
    states.foreach { state =>s"$state" in test(state)}
  }

  "Can read and write DocumentHistory actions" - {
    val actions = DocumentHistory.Action.All
    actions.foreach { action =>s"$action" in test(action)}
  }

  private def test(state: DocumentHistory.State) = {
    documentStateFormat.read(documentStateFormat.write(state)) shouldBe state
  }

  private def test(action: DocumentHistory.Action) = {
    documentActionFormat.read(documentActionFormat.write(action)) shouldBe action
  }

  "Json format for DocumentHistory should read and write correct JSON" - {
    val documentHistory = DocumentHistory(
      id = LongId(10),
      documentId = LongId(1),
      executor = xyz.driver.core.Id("userId-001"),
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
