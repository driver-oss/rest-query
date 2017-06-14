package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, UuidId}
import xyz.driver.pdsuicommon.logging._

case class RawPatientLabel(patientId: UuidId[Patient],
                           labelId: LongId[Label],
                           label: String,
                           value: FuzzyValue,
                           evidenceId: LongId[ExtractedData],
                           evidenceText: String,
                           disease: String,
                           documentId: LongId[Document],
                           requestId: RecordRequestId,
                           documentType: String,
                           providerType: String,
                           startDate: LocalDate,
                           endDate: Option[LocalDate])

object RawPatientLabel {

  implicit def toPhiString(x: RawPatientLabel): PhiString = {
    import x._
    phi"RawPatientLabel(patientId=$patientId, labelId=$labelId, label=${Unsafe(label)}, value=$value," +
      phi"evidenceId=${Unsafe(evidenceId)}, " +
      phi"evidenceText=${Unsafe(evidenceText)}, documentId=$documentId, requestId=${Unsafe(requestId)}, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, " +
      phi"startDate=$startDate, endDate=$endDate)"
  }

}
