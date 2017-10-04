package xyz.driver.pdsuidomain

import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit

import xyz.driver.pdsuicommon.BaseSuite
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities.Document

class DocumentSuite extends BaseSuite {

  "validation" - {
    "can't submit invalid data" - {
      val base = sampleDocument

      val now     = LocalDate.now()
      val past1   = now.minus(2, ChronoUnit.DAYS)
      val past2   = past1.plus(1, ChronoUnit.DAYS)
      val future1 = now.plus(1, ChronoUnit.DAYS)
      val future2 = future1.plus(1, ChronoUnit.DAYS)

      Seq(
        "startDate should be non-empty"               -> base.copy(startDate = None),
        "startDate should be greater, than endDate"   -> base.copy(startDate = Some(past2), endDate = Some(past1)),
        "startDate and endDate should be in the past" -> base.copy(startDate = Some(future1), endDate = Some(future2))
      ).foreach {
        case (title, orig) =>
          s"$title" in {
            val r = Document.validator(orig)
            assert(r.isLeft, s"should fail, but: ${r.right}")
          }
      }
    }
  }

  "getRequiredType" - {
    "getOPNType" in {
      val documentForOPNType = sampleDocument.copy(
        typeId = Some(LongId(1L)),
        providerTypeId = Some(LongId(1L)),
        startDate = Some(LocalDate.now().minus(2, ChronoUnit.DAYS))
      )
      val r = documentForOPNType.getRequiredType("Outpatient Physician Note", "Medical Oncology")
      assert(r.contains(Document.RequiredType.OPN), s"document should have the requiredType=OPN, but:$r")
    }

    "getPNType" in {
      val documentForPNType = sampleDocument.copy(
        typeId = Some(LongId(6))
      )
      val r = documentForPNType.getRequiredType("Pathology Report", "")
      assert(r.contains(Document.RequiredType.PN), s"document should have the requiredType=PN, but:$r")
    }
    "get None" in {
      val document = sampleDocument.copy(
        typeId = Some(LongId(1L)),
        providerTypeId = Some(LongId(1L)),
        startDate = Some(LocalDate.now().minus(7, ChronoUnit.MONTHS))
      )
      val r = document.getRequiredType("Outpatient Physician Note", "Medical Oncology")
      assert(r.isEmpty, s"document should have the requiredType=None, but:$r")
    }
  }

  private def sampleDocument = {
    val lastUpdate = LocalDateTime.now()

    Document(
      id = LongId(2002),
      status = Document.Status.New,
      previousStatus = None,
      assignee = None,
      previousAssignee = None,
      lastActiveUserId = None,
      recordId = LongId(2003),
      physician = None,
      typeId = Some(LongId(123)),
      providerName = Some("etst"),
      providerTypeId = Some(LongId(123)),
      requiredType = None,
      institutionName = Some("institution name"),
      startDate = Some(lastUpdate.toLocalDate.minusDays(2)),
      endDate = None,
      lastUpdate = lastUpdate,
      meta = Some(TextJson(Document.Meta(None, 1.1, 2.2)))
    )
  }
}
