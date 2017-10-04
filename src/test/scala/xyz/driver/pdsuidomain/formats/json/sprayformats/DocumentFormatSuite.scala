package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.{LocalDate, LocalDateTime}

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities.Document

class DocumentFormatSuite extends FlatSpec with Matchers {
  import document._

  "Json format for Document" should "read and write correct JSON" in {
    val orig = Document(
      id = LongId(1),
      status = Document.Status.New,
      assignee = None,
      previousStatus = None,
      previousAssignee = None,
      lastActiveUserId = None,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      recordId = LongId(101),
      physician = Some("physician"),
      typeId = Some(LongId(10)),
      providerName = Some("provider 21"),
      providerTypeId = Some(LongId(21)),
      institutionName = Some("institution name"),
      requiredType = Some(Document.RequiredType.OPN),
      meta = None,
      startDate = None,
      endDate = None
    )
    val writtenJson = documentFormat.write(orig)

    writtenJson should be (
      """{"id":1,"recordId":101,"physician":"physician","typeId":10,"provider":"provider 21","providerTypeId":21,
         "requiredType":"OPN","institutionName":"institution name","startDate":null,"endDate":null,"status":"New","assignee":null,"previousStatus":null,
         "previousAssignee":null,"lastActiveUser":null,"lastUpdate":"2017-08-10T18:00Z","meta":null}""".parseJson)

    val createDocumentJson =
      """{"recordId":101,"physician":"physician","typeId":10,"provider":"provider 21","providerTypeId":21}""".parseJson
    val expectedCreatedDocument = orig.copy(
      id = LongId(0),
      lastUpdate = LocalDateTime.MIN,
      requiredType = None,
      institutionName = None
    )
    val parsedCreatedDocument = documentFormat.read(createDocumentJson)
    parsedCreatedDocument should be(expectedCreatedDocument)

    val updateDocumentJson =
      """{"startDate":"2017-08-10","endDate":"2018-08-10","meta":{"predicted":true,"startPage":1.0,"endPage":2.0}}""".parseJson
    val expectedUpdatedDocument = orig.copy(
      startDate = Some(LocalDate.parse("2017-08-10")),
      endDate = Some(LocalDate.parse("2018-08-10")),
      meta = Some(TextJson(Document.Meta(predicted = Some(true), startPage = 1.0, endPage = 2.0)))
    )
    val parsedUpdatedDocument = applyUpdateToDocument(updateDocumentJson, orig)
    parsedUpdatedDocument should be(expectedUpdatedDocument)
  }

  "Json format for Document.Meta" should "read and write correct JSON" in {
    val meta = Document.Meta(predicted = None, startPage = 1.0, endPage = 2.0)
    val writtenJson = documentMetaFormat.write(meta)
    writtenJson should be ("""{"startPage":1.0,"endPage":2.0}""".parseJson)

    val metaJsonWithoutPredicted = """{"startPage":1.0,"endPage":2.0}""".parseJson
    val parsedMetaWithoutPredicted = documentMetaFormat.read(metaJsonWithoutPredicted)
    parsedMetaWithoutPredicted should be(meta)

    val metaJsonWithPredicted = """{"predicted":true,"startPage":1.0,"endPage":2.0}""".parseJson
    val parsedMetaWithPredicted = documentMetaFormat.read(metaJsonWithPredicted)
    parsedMetaWithPredicted should be(meta.copy(predicted = Some(true)))
  }

}
