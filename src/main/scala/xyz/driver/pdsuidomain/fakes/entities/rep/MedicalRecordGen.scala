package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.pdsuidomain.entities._
import xyz.driver.core.generators
import xyz.driver.core.generators._
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, User}
import xyz.driver.pdsuidomain.fakes.entities.common.{nextLocalDateTime, nextLongId, nextStringId, nextUuidId}

object MedicalRecordGen {
  private val maxItemsInCollectionNumber: Int = 50

  private val pageMaxNumber: Int = 1000

  private val medicalRecordMetas: Set[() => MedicalRecord.Meta] = {
    Set(
      () => nextMedicalRecordMetaReorder(),
      () => nextMedicalRecordMetaDuplicate(),
      () => nextMedicalRecordMetaRotation()
    )
  }

  def nextMedicalRecordMetas(count: Int): List[MedicalRecord.Meta] =
    List.fill(count)(nextMedicalRecordMeta())

  def nextMedicalRecordMetasJson(): TextJson[List[MedicalRecord.Meta]] =
    TextJson(nextMedicalRecordMetas(nextInt(maxItemsInCollectionNumber, minValue = 0)))

  private def nextDocument(): Document =
    DocumentGen.nextDocument()

  private def nextDocuments(count: Int): List[Document] =
    List.fill(count)(nextDocument())

  def nextDocuments(recordId: LongId[MedicalRecord]): TextJson[List[Document]] = {
    val documents = nextDocuments(
      nextInt(maxItemsInCollectionNumber, minValue = 0)
    )

    TextJson(documents.map(_.copy(recordId = recordId)))
  }

  def nextMedicalRecordStatus(): MedicalRecord.Status =
    MedicalRecord.Status.New

  def nextMedicalRecordHistoryState(): MedicalRecordHistory.State =
    generators.oneOf[MedicalRecordHistory.State](MedicalRecordHistory.State.All)

  def nextMedicalRecordHistoryAction(): MedicalRecordHistory.Action =
    generators.oneOf[MedicalRecordHistory.Action](MedicalRecordHistory.Action.All)

  def nextMedicalRecordMetaReorder(): MedicalRecord.Meta.Reorder = {
    val itemsNumber =
      maxItemsInCollectionNumber
    val items = scala.util.Random
      .shuffle(Seq.tabulate(itemsNumber)(identity))

    MedicalRecord.Meta.Reorder(
      predicted = nextOption(nextBoolean),
      items = items
    )
  }

  def nextMedicalRecordMetaDuplicate(): MedicalRecord.Meta.Duplicate = {
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

  def nextMedicalRecordMetaRotation(): MedicalRecord.Meta.Rotation = {
    val items =
      Array
        .tabulate(maxItemsInCollectionNumber)(
          index => nextString() -> index
        )
        .toMap

    MedicalRecord.Meta.Rotation(
      predicted = nextOption(nextBoolean()),
      items = items
    )
  }

  def nextMedicalRecordMeta(): MedicalRecord.Meta = {
    generators.oneOf(medicalRecordMetas)()
  }

  def nextMedicalRecord(): MedicalRecord = {
    val id = nextLongId[MedicalRecord]
    MedicalRecord(
      id = nextLongId[MedicalRecord],
      status = nextMedicalRecordStatus(),
      previousStatus = None,
      assignee = nextOption(nextStringId),
      previousAssignee = nextOption(nextStringId),
      lastActiveUserId = nextOption(nextStringId),
      patientId = nextUuidId,
      requestId = RecordRequestId(generators.nextUuid()),
      disease = generators.nextString(),
      caseId = nextOption(CaseId(generators.nextString())),
      physician = nextOption(generators.nextString()),
      meta = nextOption(nextMedicalRecordMetasJson()),
      predictedMeta = nextOption(nextMedicalRecordMetasJson()),
      predictedDocuments = nextOption(nextDocuments(id)),
      lastUpdate = nextLocalDateTime
    )
  }

  def nextMedicalRecordHistory(): MedicalRecordHistory = {
    MedicalRecordHistory(
      id = nextLongId[MedicalRecordHistory],
      executor = nextStringId[User],
      recordId = nextLongId[MedicalRecord],
      state = nextMedicalRecordHistoryState(),
      action = nextMedicalRecordHistoryAction(),
      created = nextLocalDateTime
    )
  }

  def nextMedicalRecordIssue(): MedicalRecordIssue = {
    val pages = Common.nextStartAndEndPages

    MedicalRecordIssue(
      id = nextLongId[MedicalRecordIssue],
      userId = nextStringId[User],
      recordId = nextLongId[MedicalRecord],
      startPage = pages._1,
      endPage = pages._2,
      lastUpdate = nextLocalDateTime,
      isDraft = nextBoolean(),
      text = nextString(),
      archiveRequired = nextBoolean()
    )
  }

}
