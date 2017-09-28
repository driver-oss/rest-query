package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, UuidId}
import xyz.driver.pdsuicommon.logging._

final case class RawPatientLabel(patientId: UuidId[Patient],
                                 labelId: LongId[Label],
                                 value: FuzzyValue,
                                 evidenceId: LongId[ExtractedData],
                                 evidenceText: String,
                                 disease: String,
                                 documentId: LongId[Document],
                                 requestId: RecordRequestId,
                                 documentType: DocumentType,
                                 providerType: ProviderType,
                                 startDate: LocalDate,
                                 endDate: Option[LocalDate])

object RawPatientLabel {

  implicit def toPhiString(x: RawPatientLabel): PhiString = {
    import x._
    phi"RawPatientLabel(patientId=$patientId, labelId=$labelId, value=$value, evidenceId=${Unsafe(evidenceId)}, " +
      phi"evidenceText=${Unsafe(evidenceText)}, documentId=$documentId, requestId=${Unsafe(requestId)}, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, " +
      phi"startDate=$startDate, endDate=$endDate)"
  }
}
