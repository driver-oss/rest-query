package xyz.driver.pdsuidomain.entities.export.patient

import java.time.LocalDate

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Document, RawPatientLabel, RecordRequestId}

case class ExportPatientLabelEvidenceDocument(documentId: LongId[Document],
                                              requestId: RecordRequestId,
                                              documentType: String,
                                              providerType: String,
                                              date: LocalDate)

object ExportPatientLabelEvidenceDocument extends PhiLogging {

  implicit def toPhiString(x: ExportPatientLabelEvidenceDocument): PhiString = {
    import x._
    phi"ExportPatientLabelEvidenceDocument(documentId=$documentId, requestId=$requestId, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, date=$date)"
  }

  def fromRaw(x: RawPatientLabel) = ExportPatientLabelEvidenceDocument(
    documentId = x.documentId,
    requestId = x.requestId,
    documentType = x.documentType,
    providerType = x.providerType,
    date = x.startDate
  )
}
