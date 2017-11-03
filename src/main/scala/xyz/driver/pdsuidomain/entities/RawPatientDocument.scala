package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.core.Id
import xyz.driver.entities.clinic.ClinicalRecord
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.logging._

final case class RawPatientDocument(disease: String,
                                    patientId: UuidId[Patient],
                                    requestId: Id[ClinicalRecord],
                                    recordId: LongId[MedicalRecord],
                                    recordStatus: MedicalRecord.Status,
                                    documentId: LongId[Document],
                                    documentType: String,
                                    documentProviderType: String,
                                    documentStartDate: LocalDate,
                                    documentStatus: Document.Status)

object RawPatientDocument {

  implicit def toPhiString(x: RawPatientDocument): PhiString = {
    import x._
    phi"RawPatientDocument(disease=${Unsafe(disease)}, patientId=$patientId, requestId=${Unsafe(requestId)}, " +
      phi"recordId=$recordId, recordStatus=$recordStatus, documentId=$documentId, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(documentProviderType)}, " +
      phi"startDate=$documentStartDate, documentStatus=$documentStatus)"
  }
}
