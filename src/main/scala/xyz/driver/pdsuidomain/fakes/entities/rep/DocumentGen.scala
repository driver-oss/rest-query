package xyz.driver.pdsuidomain.fakes.entities.rep

import java.time.LocalDate

import xyz.driver.core.generators
import xyz.driver.core.generators.{nextBoolean, nextDouble, nextOption, nextString}
import xyz.driver.pdsuidomain.fakes.entities.common._
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, User}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.fakes.entities.common.{nextLocalDate, nextLocalDateTime, nextLongId, nextStringId}

object DocumentGen {
  implicit private class LocalDateOrdering(localData: LocalDate) extends Ordered[LocalDate] {

    override def compare(that: LocalDate): Int = {
      this.localData.compareTo(that)
    }
  }

  private def nextDates() =
    genBoundedRangeOption[LocalDate](nextLocalDate, nextLocalDate)

  private def nextStartAndEndPagesOption() =
    nextStartAndEndPages

  private def nextStartAndEndPage() =
    genBoundedRange(nextDouble(), nextDouble())

  def nextDocumentStatus(): Document.Status =
    Document.Status.New

  def nextDocumentRequiredType(): Document.RequiredType =
    generators.oneOf[Document.RequiredType](Document.RequiredType.All)

  def nextDocumentHistoryState(): DocumentHistory.State =
    generators.oneOf[DocumentHistory.State](DocumentHistory.State.All)

  def nextDocumentHistoryAction(): DocumentHistory.Action =
    generators.oneOf[DocumentHistory.Action](DocumentHistory.Action.All)

  def nextDocumentMeta(): Document.Meta = {
    val (startPage, endPage) = nextStartAndEndPage()
    Document.Meta(startPage, endPage)
  }

  def nextDocumentMetaJson(): TextJson[Document.Meta] = {
    TextJson(nextDocumentMeta())
  }

  def nextDocument(): Document = {
    val dates = nextDates()

    Document(
      id = nextLongId[Document],
      status = nextDocumentStatus(),
      previousStatus = None,
      assignee = nextOption(nextStringId[User]),
      previousAssignee = nextOption(nextStringId[User]),
      lastActiveUserId = nextOption(nextStringId[User]),
      recordId = nextLongId[MedicalRecord],
      physician = nextOption(nextString()),
      typeId = nextOption(nextLongId[DocumentType]),
      providerName = nextOption(nextString()),
      providerTypeId = nextOption(nextLongId[ProviderType]),
      requiredType = nextOption(nextDocumentRequiredType()),
      institutionName = nextOption(nextString()),
      meta = nextOption(nextDocumentMetaJson()),
      startDate = dates._1,
      endDate = dates._2,
      lastUpdate = nextLocalDateTime,
      labelVersion = generators.nextInt(1, 10)
    )
  }

  def nextDocumentType(): DocumentType =
    generators.oneOf(DocumentType.All: _*)

  def nextDocumentIssue(documentId: LongId[Document] = nextLongId): DocumentIssue = {
    val pages = nextStartAndEndPagesOption()

    DocumentIssue(
      id = nextLongId[DocumentIssue],
      userId = nextStringId[User],
      documentId = documentId,
      startPage = pages._1,
      endPage = pages._2,
      lastUpdate = nextLocalDateTime,
      isDraft = nextBoolean(),
      text = nextString(),
      archiveRequired = nextBoolean()
    )
  }

  def nextDocumentHistory(documentId: LongId[Document] = nextLongId): DocumentHistory = {
    DocumentHistory(
      id = nextLongId[DocumentHistory],
      executor = nextStringId[User],
      documentId = documentId,
      state = nextDocumentHistoryState(),
      action = nextDocumentHistoryAction(),
      created = nextLocalDateTime
    )
  }
}
