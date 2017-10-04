package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime
import java.util.UUID

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.entities.MedicalRecord.Status
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.sprayformats.record.recordFormat


class ListResponseSuite extends FlatSpec with Matchers {

  private val lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00")
  private val lastUpdateToLocal = "2017-08-10T18:00Z"

  private def metaJsonObjectAsString(meta: ListResponse.Meta) = {
    import meta._
    val lastUpdate = meta.lastUpdate
      .map(_ => s""","lastUpdate":"$lastUpdateToLocal"""")
      .getOrElse("")

    s"""{"itemsCount":$itemsCount,"pageNumber":$pageNumber,"pageSize":$pageSize$lastUpdate}"""
  }


  "Json format for ListResponse.Meta" should "read and write correct JSON" in {
    val meta1 =
      ListResponse.Meta(
        itemsCount = 5,
        pageNumber = 6,
        pageSize = 7,
        lastUpdate = None
      )

    val writtenJson1 =
      ListResponse.listResponseMetaFormat.write(meta1)

    writtenJson1 should be(
      metaJsonObjectAsString(meta1).parseJson)

    val parsedItem1: ListResponse.Meta =
      ListResponse.listResponseMetaFormat.read(writtenJson1)

    meta1 shouldBe parsedItem1


    val meta2 =
      ListResponse.Meta(
        itemsCount = 1,
        pageNumber = 4,
        pageSize = 3,
        lastUpdate = Some(lastUpdate)
      )

    val writtenJson2 =
      ListResponse.listResponseMetaFormat.write(meta2)

    writtenJson2 should be(
      metaJsonObjectAsString(meta2).parseJson)

    val parsedItem2: ListResponse.Meta =
      ListResponse.listResponseMetaFormat.read(writtenJson2)

    meta2 shouldBe parsedItem2
  }


  "Json format for ListResponse" should "write correct JSON" in {

    val orig = MedicalRecord(
      id = LongId(1),
      status = Status.New,
      assignee = None,
      previousStatus = None,
      previousAssignee = None,
      lastActiveUserId = None,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      physician = Some("physician"),
      meta = None,
      predictedMeta = None,
      predictedDocuments = None,
      disease = "Breast",
      requestId = RecordRequestId(UUID.fromString("7b54a75d-4197-4b27-9045-b9b6cb131be9")),
      caseId = None,
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343")
    )

    val recordJsonAsString =
      """{"id":1,"status":"New","assignee":null,"previousStatus":null,"previousAssignee":null,"lastActiveUser":null,
        "lastUpdate":"2017-08-10T18:00Z","meta":[],"patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","caseId":null,
        "requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9","disease":"Breast","physician":"physician"}"""

    val meta =
      ListResponse.Meta(
        itemsCount = 5,
        pageNumber = 6,
        pageSize = 7,
        lastUpdate = None
      )

    val listResponse = ListResponse(Seq(orig), meta)

    val writtenJson = ListResponse.listResponseMetaWriter.write(listResponse)
    val expectedJson = s"""{"items":[$recordJsonAsString],"meta":${metaJsonObjectAsString(meta)}}"""

    writtenJson should be(
      expectedJson.parseJson)
  }

}
