package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.core.Id
import xyz.driver.entities.clinic.ClinicalRecord
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, UuidId}
import xyz.driver.pdsuidomain.entities.MedicalRecord

class MedicalRecordFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.record._
  import MedicalRecord._

  "Json format for MedicalRecord.Meta" should "read and write correct JSON" in {
    val duplicate1 = Meta.Duplicate(
      startPage = 1.0d,
      endPage = 2.0d,
      startOriginalPage = 1.0d,
      endOriginalPage = Some(2.0d)
    )

    val duplicate2 = Meta.Duplicate(
      startPage = 1.0d,
      endPage = 2.0d,
      startOriginalPage = 1.0d,
      endOriginalPage = None
    )

    val reorder = Meta.Reorder(
      Seq(1, 2)
    )

    val rotation = Meta.Rotation(
      Map("item1" -> 1, "item2" -> 2)
    )

    val writtenDuplicateJson1 =
      duplicateMetaFormat.write(duplicate1)

    val writtenDuplicateJson2 =
      duplicateMetaFormat.write(duplicate2)

    val writtenReorderJson =
      reorderMetaFormat.write(reorder)

    val writtenRotationJson =
      rotateMetaFormat.write(rotation)

    writtenDuplicateJson1 should be(
      """{"startOriginalPage":1.0,"endPage":2.0,"startPage":1.0,"type":"duplicate","endOriginalPage":2.0}""".parseJson)

    writtenDuplicateJson2 should be(
      """{"startOriginalPage":1.0,"endPage":2.0,"startPage":1.0,"type":"duplicate","endOriginalPage":null}""".parseJson)

    writtenReorderJson should be(
      """{"type":"reorder","items":[1,2]}""".parseJson)

    writtenRotationJson should be(
      """{"type":"rotation","items":{"item1":1,"item2":2}}""".parseJson)

    val parsedDuplicateJson1 =
      duplicateMetaFormat.read(writtenDuplicateJson1)

    val parsedDuplicateJson2 =
      duplicateMetaFormat.read(writtenDuplicateJson2)

    val parsedReorderJson =
      reorderMetaFormat.read(writtenReorderJson)

    val parsedRotationJson =
      rotateMetaFormat.read(writtenRotationJson)

    duplicate1 should be(parsedDuplicateJson1)

    duplicate2 should be(parsedDuplicateJson2)

    reorder should be(parsedReorderJson)

    rotation should be(parsedRotationJson)

    duplicate1 should be(recordMetaTypeFormat.read(recordMetaTypeFormat.write(duplicate1)))
    duplicate2 should be(recordMetaTypeFormat.read(recordMetaTypeFormat.write(duplicate2)))
    reorder should be(recordMetaTypeFormat.read(recordMetaTypeFormat.write(reorder)))
    rotation should be(recordMetaTypeFormat.read(recordMetaTypeFormat.write(rotation)))
  }


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
      requestId = Id[ClinicalRecord]("7b54a75d-4197-4b27-9045-b9b6cb131be9"),
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
      requestId = Id[ClinicalRecord]("7b54a75d-4197-4b27-9045-b9b6cb131be9"),
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
