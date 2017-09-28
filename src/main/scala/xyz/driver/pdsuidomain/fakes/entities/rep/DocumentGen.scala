package xyz.driver.pdsuidomain.fakes.entities.rep


import java.time.LocalDate

import xyz.driver.core.generators
import xyz.driver.core.generators.{nextBoolean, nextDouble, nextOption, nextString}
import xyz.driver.pdsuicommon.domain.{TextJson, User}
import xyz.driver.pdsuidomain.entities.{Document, DocumentType, MedicalRecord, ProviderType}
import xyz.driver.pdsuidomain.fakes.entities.common.{nextLocalDate, nextLocalDateTime, nextLongId, nextStringId}

object DocumentGen {
  implicit private class LocalDateOrdering(localData: LocalDate)
    extends Ordered[LocalDate] {

    override def compare(that: LocalDate): Int = {
      this.localData.compareTo(that)
    }
  }

  def nextDocumentStatus: Document.Status =
    generators.oneOf[Document.Status](Document.Status.All)

  def nextDocumentRequiredType: Document.RequiredType =
    generators.oneOf[Document.RequiredType](Document.RequiredType.All)

  def nextDocumentMeta: Document.Meta = {
    val (startPage, endPage) = {
      val startPage = nextDouble()
      val endPage = nextDouble()
      if (startPage > endPage) {
        endPage -> startPage
      }
      else {
        startPage -> endPage
      }
    }

    Document.Meta(
      nextOption(nextBoolean()), startPage, endPage
    )
  }

  def nextDocument: Document = {
    val dates = Common
      .genBoundedRangeOption[LocalDate](nextLocalDate, nextLocalDate)

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

}
