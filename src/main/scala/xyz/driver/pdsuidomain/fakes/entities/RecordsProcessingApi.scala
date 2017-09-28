package xyz.driver.pdsuidomain.fakes.entities


import xyz.driver.pdsuidomain.entities._
import xyz.driver.core.generators
import xyz.driver.core.generators._
import common._
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.domain.{TextJson, User}
import xyz.driver.pdsuidomain.fakes.entities.rep.{DocumentGen, MedicalRecordMetaGen, Common}


object RecordsProcessingApi {
  private val maxCollectionNumber = 5

  private val maxAttemtsNumber = 100

  private def nextMedicalRecordMetasJson: TextJson[List[MedicalRecord.Meta]] =
    TextJson(nextMedicalRecordMetas(nextInt(maxCollectionNumber, minValue = 0)))

  private def nextDocuments: TextJson[List[Document]] =
    TextJson(nextDocuments(nextInt(maxCollectionNumber, minValue = 0)))

  def nextMedicalRecordStatus: MedicalRecord.Status =
    generators.oneOf[MedicalRecord.Status](MedicalRecord.Status.All)

  def nextMedicalRecordMeta: MedicalRecord.Meta =
    MedicalRecordMetaGen.nextMedicalRecordMeta

  def nextMedicalRecordMetas(count: Int): List[MedicalRecord.Meta] =
    List.fill(count)(nextMedicalRecordMeta)

  def nextMedicalRecordHistoryState: MedicalRecordHistory.State =
    generators.oneOf[MedicalRecordHistory.State](MedicalRecordHistory.State.All)

  def nextMedicalRecordHistoryAction: MedicalRecordHistory.Action =
    generators.oneOf[MedicalRecordHistory.Action](MedicalRecordHistory.Action.All)

  def nextDocument: Document =
    DocumentGen.nextDocument

  def nextDocuments(count: Int): List[Document] =
    List.fill(count)(nextDocument)

  def nextMedicalRecord() = {
    MedicalRecord(
      id = nextLongId[MedicalRecord],
      status = nextMedicalRecordStatus,
      previousStatus = nextOption(nextMedicalRecordStatus),
      assignee = nextOption(nextStringId),
      previousAssignee = nextOption(nextStringId),
      lastActiveUserId = nextOption(nextStringId),
      patientId = nextUuidId,
      requestId = RecordRequestId(generators.nextUuid()),
      disease = generators.nextString(),
      caseId = nextOption(CaseId(generators.nextString())),
      physician = nextOption(generators.nextString()),
      meta = nextOption(nextMedicalRecordMetasJson),
      predictedMeta = nextOption(nextMedicalRecordMetasJson),
      predictedDocuments = nextOption(nextDocuments),
      lastUpdate = nextLocalDateTime
    )
  }

  def nextMedicalRecordHistory() = {
    MedicalRecordHistory(
      id = nextLongId[MedicalRecordHistory],
      executor = nextStringId[User],
      recordId = nextLongId[MedicalRecord],
      state = nextMedicalRecordHistoryState,
      action = nextMedicalRecordHistoryAction,
      created = nextLocalDateTime
    )
  }

  def nextMedicalRecordIssue(): MedicalRecordIssue = {
    val pages = Common.genStartAndEndPages

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

  def nextBridgeUploadQueueItem(): BridgeUploadQueue.Item = {
    BridgeUploadQueue.Item(
      kind = nextString(),
      tag = nextString(),
      created = nextLocalDateTime,
      attempts = nextInt(maxAttemtsNumber, minValue = 0),
      nextAttempt = nextLocalDateTime,
      completed = nextBoolean(),
      dependencyKind = nextOption(nextString()),
      dependencyTag = nextOption(nextString())
    )
  }

  def nextProviderType(): ProviderType = {
    ProviderType(
      id = nextLongId[ProviderType],
      name = nextString()
    )
  }

}
