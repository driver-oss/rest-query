package xyz.driver.pdsuidomain.fakes.entities.rep


import java.time.LocalDate

import xyz.driver.core.generators
import xyz.driver.core.generators.{nextBoolean, nextDouble, nextOption, nextString}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, User}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.fakes.entities.common.{nextLocalDate, nextLocalDateTime, nextLongId, nextStringId}

object DocumentGen {
  implicit private class LocalDateOrdering(localData: LocalDate)
    extends Ordered[LocalDate] {

    override def compare(that: LocalDate): Int = {
      this.localData.compareTo(that)
    }
  }

  private def nextDates =
    Common.genBoundedRangeOption[LocalDate](nextLocalDate, nextLocalDate)

  private def nextStartAndEndPagesOption =
    Common.nextStartAndEndPages

  private def nextStartAndEndPage =
    Common.genBoundedRange(nextDouble(),nextDouble())


  def nextDocumentStatus: Document.Status =
    generators.oneOf[Document.Status](Document.Status.All)

  def nextDocumentRequiredType: Document.RequiredType =
    generators.oneOf[Document.RequiredType](Document.RequiredType.All)

  def nextDocumentHistoryState: DocumentHistory.State =
    generators.oneOf[DocumentHistory.State](DocumentHistory.State.All)

  def nextDocumentHistoryAction: DocumentHistory.Action =
    generators.oneOf[DocumentHistory.Action](DocumentHistory.Action.All)

  def nextDocumentMeta: Document.Meta = {
    val (startPage, endPage) = nextStartAndEndPage

    Document.Meta(
      nextOption(nextBoolean()), startPage, endPage
    )
  }

  def nextDocument: Document = {
    val dates = nextDates

    Document(
      id = nextLongId[Document],
      status = nextDocumentStatus,
      previousStatus = nextOption(nextDocumentStatus),
      assignee = nextOption(nextStringId[User]),
      previousAssignee = nextOption(nextStringId[User]),
      lastActiveUserId = nextOption(nextStringId[User]),
      recordId = nextLongId[MedicalRecord],
      physician = nextOption(nextString()),
      typeId = nextOption(nextLongId[DocumentType]),
      providerName = nextOption(nextString()),
      providerTypeId = nextOption(nextLongId[ProviderType]),
      requiredType = nextOption(nextDocumentRequiredType),
      meta = nextOption(TextJson(nextDocumentMeta)),
      startDate = dates._1,
      endDate = dates._2,
      lastUpdate = nextLocalDateTime
    )
  }

  def nextDocumentType: DocumentType = {
    DocumentType(
      id = nextLongId[DocumentType],
      name = nextString()
    )
  }

  def nextDocumentIssue(documentId: LongId[Document]): DocumentIssue = {
    val pages = nextStartAndEndPagesOption

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


  def nextDocumentHistory(documentId: LongId[Document]): DocumentHistory = {
    DocumentHistory(
      id = nextLongId[DocumentHistory],
      executor = nextStringId[User],
      documentId = documentId,
      state = nextDocumentHistoryState,
      action = nextDocumentHistoryAction,
      created = nextLocalDateTime
    )
  }
}
