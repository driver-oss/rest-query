package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.pdsuidomain.entities.MedicalRecord
import xyz.driver.core.generators
import xyz.driver.core.generators._

object MedicalRecordMetaGen {
  private val maxItemsInCollectionNumber = 50
  private val pageMaxNumber = 1000

  private val medicalRecordMetas = {
    Set(
      () => nextMedicalRecordMetaReorder,
      () => nextMedicalRecordMetaDuplicate,
      () => nextMedicalRecordMetaRotation
    )
  }


  def nextMedicalRecordMetaReorder: MedicalRecord.Meta.Reorder = {
    val itemsNumber =
      maxItemsInCollectionNumber
    val items = scala.util.Random
      .shuffle(Seq.tabulate(itemsNumber)(identity))

    MedicalRecord.Meta.Reorder(
      predicted = nextOption(nextBoolean),
      items = items
    )
  }


  def nextMedicalRecordMetaDuplicate: MedicalRecord.Meta.Duplicate = {
    val startPageGen =
      nextInt(pageMaxNumber, minValue = 0)
    val endPageGen =
      nextInt(pageMaxNumber, startPageGen)

    MedicalRecord.Meta.Duplicate(
      predicted = nextOption(nextBoolean),
      startPage = startPageGen.toDouble,
      endPage = endPageGen.toDouble,
      startOriginalPage = startPageGen.toDouble,
      endOriginalPage = nextOption(endPageGen.toDouble)
    )
  }

  def nextMedicalRecordMetaRotation: MedicalRecord.Meta.Rotation = {
    val items =
      Array.tabulate(maxItemsInCollectionNumber)(
        index => nextString() -> index
      ).toMap

    MedicalRecord.Meta.Rotation(
      predicted = nextOption(nextBoolean()),
      items = items
    )
  }

  def nextMedicalRecordMeta: MedicalRecord.Meta = {
    generators.oneOf(medicalRecordMetas)()
  }
}
