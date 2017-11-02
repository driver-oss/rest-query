package xyz.driver.pdsuidomain.entities.export.patient

import java.time.LocalDate

import xyz.driver.entities.clinic.ClinicalRecord
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

final case class ExportPatientLabelEvidenceDocument(documentId: LongId[Document],
                                                    requestId: xyz.driver.core.Id[ClinicalRecord],
                                                    documentType: DocumentType,
                                                    providerType: ProviderType,
                                                    date: LocalDate)

object ExportPatientLabelEvidenceDocument extends PhiLogging {

  implicit def toPhiString(x: ExportPatientLabelEvidenceDocument): PhiString = {
    import x._
    phi"ExportPatientLabelEvidenceDocument(documentId=$documentId, requestId=${Unsafe(requestId)}, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, date=$date)"
  }
}
