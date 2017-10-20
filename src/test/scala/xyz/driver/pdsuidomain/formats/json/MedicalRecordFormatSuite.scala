package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime
import java.util.UUID

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, UuidId}
import xyz.driver.pdsuidomain.entities.{MedicalRecord, RecordRequestId}

class MedicalRecordFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.record._
  import MedicalRecord._

  "Json format for MedicalRecord" should "read and write correct JSON" in {
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
      disease = "Breast",
      requestId = RecordRequestId(UUID.fromString("7b54a75d-4197-4b27-9045-b9b6cb131be9")),
      caseId = None,
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      totalPages = 10
    )
    val writtenJson = recordFormat.write(orig)

    writtenJson should be(
      """{"id":1,"status":"New","assignee":null,"previousStatus":null,"previousAssignee":null,"lastActiveUser":null,
        "lastUpdate":"2017-08-10T18:00Z","meta":[],"patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","caseId":null,
        "requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9","disease":"Breast","physician":"physician","totalPages":10}""".parseJson)

    val createRecordJson =
      """{"disease":"Breast","patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9"}""".parseJson
    val expectedCreatedRecord = MedicalRecord(
      id = LongId(0),
      status = MedicalRecord.Status.New,
      previousStatus = None,
      assignee = None,
      previousAssignee = None,
      lastActiveUserId = None,
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      requestId = RecordRequestId(UUID.fromString("7b54a75d-4197-4b27-9045-b9b6cb131be9")),
      disease = "Breast",
      caseId = None,
      physician = None,
      meta = None,
      lastUpdate = LocalDateTime.now(),
      totalPages = 0
    )
    val parsedCreatedRecord = recordFormat.read(createRecordJson).copy(lastUpdate = expectedCreatedRecord.lastUpdate)
    parsedCreatedRecord should be(expectedCreatedRecord)

    val updateRecordJson =
      """{"meta":[{"type":"duplicate","startPage":1.0,"endPage":2.0,"startOriginalPage":1.0},
        {"type":"reorder","items":[1,2]},
        {"type":"rotation","items":{"item1":1,"item2":2}}]}""".parseJson
    val expectedUpdatedRecord = orig.copy(
      meta = Some(
        TextJson(
          List(
            Meta.Duplicate(startPage = 1.0, endPage = 2.0, startOriginalPage = 1.0, endOriginalPage = None),
            Meta.Reorder(Seq(1, 2)),
            Meta.Rotation(Map("item1" -> 1, "item2" -> 2))
          )))
    )
    val parsedUpdatedRecord = applyUpdateToMedicalRecord(updateRecordJson, orig)
    parsedUpdatedRecord should be(expectedUpdatedRecord)
  }

}
