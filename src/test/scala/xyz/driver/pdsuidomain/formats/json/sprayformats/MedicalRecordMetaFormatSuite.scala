package xyz.driver.pdsuidomain.formats.json.sprayformats

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import xyz.driver.pdsuidomain.entities.MedicalRecord.Meta
import record._

class MedicalRecordMetaFormatSuite
  extends FlatSpec
    with Matchers {

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

}
