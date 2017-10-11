package xyz.driver.pdsuidomain.entities.export.patient

import java.time.LocalDate

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

final case class ExportPatientLabelEvidenceDocument(documentId: LongId[Document],
                                                    requestId: RecordRequestId,
                                                    documentType: DocumentType,
                                                    providerType: ProviderType,
                                                    date: LocalDate)

object ExportPatientLabelEvidenceDocument extends PhiLogging {

  implicit def toPhiString(x: ExportPatientLabelEvidenceDocument): PhiString = {
    import x._
    phi"ExportPatientLabelEvidenceDocument(documentId=$documentId, requestId=$requestId, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, date=$date)"
  }
}
