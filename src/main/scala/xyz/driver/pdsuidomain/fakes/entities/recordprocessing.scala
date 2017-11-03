package xyz.driver.pdsuidomain.fakes.entities

import java.time.LocalDate

import xyz.driver.core.auth.User
import xyz.driver.core.generators
import xyz.driver.core.generators._
import xyz.driver.entities.assays.PatientCase
import xyz.driver.entities.clinic.ClinicalRecord
import xyz.driver.entities.labels.{Label, LabelCategory, LabelValue}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.ExtractedData.Meta
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.fakes.entities.common._
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

object recordprocessing {

  private val maxItemsInCollectionNumber: Int = 50
  private val maxPageNumber                   = 100
  private val maxIndexNumber                  = 100
  private val maxOffsetNumber                 = 10

  implicit private class LocalDateOrdering(localData: LocalDate) extends Ordered[LocalDate] {
    override def compare(that: LocalDate): Int = {
      this.localData.compareTo(that)
    }
  }

  implicit private class TextLayerPositionOrdering(textLayerPosition: ExtractedData.Meta.TextLayerPosition)
      extends Ordered[ExtractedData.Meta.TextLayerPosition] {
    override def compare(that: Meta.TextLayerPosition): Int = {
      if (this.textLayerPosition.page < that.page) -1
      else if (this.textLayerPosition.page > that.page) 1
      else if (this.textLayerPosition.index < that.index) -1
      else if (this.textLayerPosition.index > that.index) 1
      else if (this.textLayerPosition.offset < that.offset) -1
      else if (this.textLayerPosition.offset > that.offset) 1
      else 0
    }
  }

  private def nextDates(): (Option[LocalDate], Option[LocalDate]) =
    genBoundedRangeOption[LocalDate](nextLocalDate, nextLocalDate)

  private val medicalRecordMeta: Set[() => MedicalRecord.Meta] = {
    Set(
      () => nextMedicalRecordMetaReorder(),
      () => nextMedicalRecordMetaDuplicate(),
      () => nextMedicalRecordMetaRotation()
    )
  }

  def nextMedicalRecordMeta(count: Int): List[MedicalRecord.Meta] =
    List.fill(count)(nextMedicalRecordMeta())

  def nextMedicalRecordMetaJson(): TextJson[List[MedicalRecord.Meta]] =
    TextJson(nextMedicalRecordMeta(nextInt(maxItemsInCollectionNumber)))

  private def nextDocumentList(count: Int): List[Document] =
    List.fill(count)(nextDocument())

  def nextDocumentList(recordId: LongId[MedicalRecord]): TextJson[List[Document]] = {
    val documents = nextDocumentList(
      nextInt(maxItemsInCollectionNumber)
    )
    TextJson(documents.map(_.copy(recordId = recordId)))
  }

  def nextMedicalRecordStatus(): MedicalRecord.Status =
    generators.oneOf[MedicalRecord.Status](MedicalRecord.Status.All)

  def nextMedicalRecordHistoryState(): MedicalRecordHistory.State =
    generators.oneOf[MedicalRecordHistory.State](MedicalRecordHistory.State.All)

  def nextMedicalRecordHistoryAction(): MedicalRecordHistory.Action =
    generators.oneOf[MedicalRecordHistory.Action](MedicalRecordHistory.Action.All)

  def nextMedicalRecordMetaReorder(): MedicalRecord.Meta.Reorder = {
    val itemsNumber = maxItemsInCollectionNumber
    val items       = scala.util.Random.shuffle(Seq.tabulate(itemsNumber)(identity))

    MedicalRecord.Meta.Reorder(items)
  }

  def nextMedicalRecordMetaDuplicate(): MedicalRecord.Meta.Duplicate = {
    val startPageGen = nextInt(maxPageNumber)
    val endPageGen   = nextInt(maxPageNumber, startPageGen)

    MedicalRecord.Meta.Duplicate(
      startPage = startPageGen.toDouble,
      endPage = endPageGen.toDouble,
      startOriginalPage = startPageGen.toDouble,
      endOriginalPage = nextOption(endPageGen.toDouble)
    )
  }

  def nextMedicalRecordMetaRotation(): MedicalRecord.Meta.Rotation = {
    val items = Array.tabulate(maxItemsInCollectionNumber)(index => nextString() -> index).toMap

    MedicalRecord.Meta.Rotation(items = items)
  }

  def nextMedicalRecordMeta(): MedicalRecord.Meta = generators.oneOf(medicalRecordMeta)()

  def nextMedicalRecord(): MedicalRecord = MedicalRecord(
    id = nextLongId[MedicalRecord],
    status = nextMedicalRecordStatus(),
    previousStatus = nextOption(generators.oneOf[MedicalRecord.Status](MedicalRecord.Status.AllPrevious)),
    assignee = nextOption(generators.nextId[User]),
    previousAssignee = nextOption(generators.nextId[User]),
    lastActiveUserId = nextOption(generators.nextId[User]),
    patientId = nextUuidId[Patient],
    requestId = generators.nextId[ClinicalRecord](),
    disease = generators.nextString(),
    caseId = nextOption(generators.nextId[PatientCase]()),
    physician = nextOption(generators.nextString()),
    meta = nextOption(nextMedicalRecordMetaJson()),
    lastUpdate = nextLocalDateTime,
    totalPages = nextInt(10)
  )

  def nextMedicalRecordHistory(): MedicalRecordHistory = MedicalRecordHistory(
    id = nextLongId[MedicalRecordHistory],
    executor = generators.nextId[User],
    recordId = nextLongId[MedicalRecord],
    state = nextMedicalRecordHistoryState(),
    action = nextMedicalRecordHistoryAction(),
    created = nextLocalDateTime
  )

  def nextMedicalRecordIssue(): MedicalRecordIssue = {
    val (startPage, endPage) = nextStartAndEndPagesOption

    MedicalRecordIssue(
      id = nextLongId[MedicalRecordIssue],
      userId = generators.nextId[User],
      recordId = nextLongId[MedicalRecord],
      startPage = startPage,
      endPage = endPage,
      lastUpdate = nextLocalDateTime,
      isDraft = nextBoolean(),
      text = nextString(),
      archiveRequired = nextBoolean()
    )
  }

  def nextDocumentStatus(): Document.Status = generators.oneOf[Document.Status](Document.Status.All)

  def nextDocumentRequiredType(): Document.RequiredType =
    generators.oneOf[Document.RequiredType](Document.RequiredType.All)

  def nextDocumentHistoryState(): DocumentHistory.State =
    generators.oneOf[DocumentHistory.State](DocumentHistory.State.All)

  def nextDocumentHistoryAction(): DocumentHistory.Action =
    generators.oneOf[DocumentHistory.Action](DocumentHistory.Action.All)

  def nextDocumentMeta(): Document.Meta = {
    val (startPage, endPage) = nextStartAndEndPages
    Document.Meta(startPage, endPage)
  }

  def nextDocumentMetaJson(): TextJson[Document.Meta] = nextTextJson(nextDocumentMeta())

  def nextDocument(): Document = {
    val (startDate, endDate) = nextDates()

    Document(
      id = nextLongId[Document],
      status = nextDocumentStatus(),
      previousStatus = nextOption(generators.oneOf[Document.Status](Document.Status.AllPrevious)),
      assignee = nextOption(generators.nextId[User]),
      previousAssignee = nextOption(generators.nextId[User]),
      lastActiveUserId = nextOption(generators.nextId[User]),
      recordId = nextLongId[MedicalRecord],
      physician = nextOption(nextString()),
      typeId = nextOption(nextLongId[DocumentType]),
      providerName = nextOption(nextString()),
      providerTypeId = nextOption(nextLongId[ProviderType]),
      requiredType = nextOption(nextDocumentRequiredType()),
      institutionName = nextOption(nextString()),
      meta = nextOption(nextDocumentMetaJson()),
      startDate = startDate,
      endDate = endDate,
      lastUpdate = nextLocalDateTime,
      labelVersion = generators.nextInt(100)
    )
  }

  def nextDocumentIssue(): DocumentIssue = {
    val (startPage, endPage) = nextStartAndEndPagesOption
    DocumentIssue(
      id = nextLongId[DocumentIssue],
      userId = generators.nextId[User],
      documentId = nextLongId[Document],
      startPage = startPage,
      endPage = endPage,
      lastUpdate = nextLocalDateTime,
      isDraft = nextBoolean(),
      text = nextString(),
      archiveRequired = nextBoolean()
    )
  }

  def nextDocumentHistory(): DocumentHistory = DocumentHistory(
    id = nextLongId[DocumentHistory],
    executor = generators.nextId[User],
    documentId = nextLongId[Document],
    state = nextDocumentHistoryState(),
    action = nextDocumentHistoryAction(),
    created = nextLocalDateTime
  )

  def nextExtractedDataMetaKeyword(): Meta.Keyword = {
    ExtractedData.Meta.Keyword(
      page = nextInt(maxPageNumber),
      pageRatio = nextOption(nextDouble()),
      index = nextInt(maxIndexNumber),
      sortIndex = nextString()
    )
  }

  def nextExtractedDataMetaTextLayerPosition(): Meta.TextLayerPosition = {
    ExtractedData.Meta.TextLayerPosition(
      page = nextInt(maxPageNumber),
      index = nextInt(maxIndexNumber),
      offset = nextInt(maxOffsetNumber)
    )
  }

  def nextExtractedDataMetaEvidence(): Meta.Evidence = {
    val (start, end) =
      genBoundedRange[ExtractedData.Meta.TextLayerPosition](
        nextExtractedDataMetaTextLayerPosition(),
        nextExtractedDataMetaTextLayerPosition()
      )

    ExtractedData.Meta.Evidence(
      pageRatio = nextDouble(),
      start = start,
      end = end
    )
  }

  def nextExtractedDataMeta(): Meta = {
    ExtractedData.Meta(
      nextOption(nextExtractedDataMetaKeyword()),
      nextOption(nextExtractedDataMetaEvidence())
    )
  }

  def nextExtractedDataMetaJson(): TextJson[Meta] =
    nextTextJson(nextExtractedDataMeta())

  def nextExtractedData(): ExtractedData = {
    ExtractedData(
      id = nextLongId[ExtractedData],
      documentId = nextLongId[Document],
      keywordId = nextOption(nextLongId[Keyword]),
      evidenceText = nextOption(nextString()),
      meta = nextOption(nextExtractedDataMetaJson())
    )
  }

  def nextExtractedDataLabel(dataId: LongId[ExtractedData]): ExtractedDataLabel = {
    ExtractedDataLabel(
      id = nextLongId[ExtractedDataLabel],
      dataId = nextLongId[ExtractedData],
      labelId = nextOption(nextLongId[Label]),
      categoryId = nextOption(nextLongId[LabelCategory]),
      value = nextOption(generators.oneOf[LabelValue](LabelValue.Yes, LabelValue.No, LabelValue.Maybe))
    )
  }

  def nextRichExtractedData(): RichExtractedData = {
    val extractedData = nextExtractedData()
    RichExtractedData(
      extractedData = extractedData,
      labels = List.fill(
        nextInt(maxItemsInCollectionNumber)
      )(nextExtractedDataLabel(extractedData.id))
    )
  }

  def nextMedicalRecordListResponse(): ListResponse[MedicalRecord] = {
    val xs: Seq[MedicalRecord] = Seq.fill(3)(nextMedicalRecord())
    nextListResponse(xs)
  }

  def nextMedicalRecordIssueListResponse(): ListResponse[MedicalRecordIssue] = {
    val xs: Seq[MedicalRecordIssue] = Seq.fill(3)(nextMedicalRecordIssue())
    nextListResponse(xs)
  }

  def nextMedicalRecordHistoryListResponse(): ListResponse[MedicalRecordHistory] = {
    val xs: Seq[MedicalRecordHistory] = Seq.fill(3)(nextMedicalRecordHistory())
    nextListResponse(xs)
  }

  def nextDocumentListResponse(): ListResponse[Document] = {
    val xs: Seq[Document] = Seq.fill(3)(nextDocument())
    nextListResponse(xs)
  }

  def nextDocumentIssueListResponse(): ListResponse[DocumentIssue] = {
    val xs: Seq[DocumentIssue] = Seq.fill(3)(nextDocumentIssue())
    nextListResponse(xs)
  }

  def nextDocumentHistoryListResponse(): ListResponse[DocumentHistory] = {
    val xs: Seq[DocumentHistory] = Seq.fill(3)(nextDocumentHistory())
    nextListResponse(xs)
  }

  def nextRichExtractedDataListResponse(): ListResponse[RichExtractedData] = {
    val xs: Seq[RichExtractedData] = Seq.fill(3)(nextRichExtractedData())
    nextListResponse(xs)
  }

}
