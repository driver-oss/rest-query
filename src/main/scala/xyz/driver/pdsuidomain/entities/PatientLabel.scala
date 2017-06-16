package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, UuidId}
import xyz.driver.pdsuicommon.logging._

object PatientLabel {
  implicit def toPhiString(x: PatientLabel): PhiString = {
    import x._
    phi"PatientLabel(id=$id, patientId=$patientId, labelId=$labelId, " +
      phi"score=${Unsafe(score)}, primaryValue=${Unsafe(primaryValue)}, " +
      phi"verifiedPrimaryValue=${Unsafe(verifiedPrimaryValue)})"
  }
}

final case class PatientLabel(id: LongId[PatientLabel],
                              patientId: UuidId[Patient],
                              labelId: LongId[Label],
                              score: Int,
                              primaryValue: Option[FuzzyValue],
                              verifiedPrimaryValue: Option[FuzzyValue],
                              isImplicitMatch: Boolean,
                              isVisible: Boolean)

object PatientLabelEvidence {
  implicit def toPhiString(x: PatientLabelEvidence): PhiString = {
    import x._
    phi"PatientLabelEvidence(id=$id, patientLabelId=$patientLabelId, value=${Unsafe(value)}, " +
      phi"documentId=$documentId, evidenceId=$evidenceId, reportId=$reportId)"
  }
}

case class PatientLabelEvidence(id: LongId[PatientLabelEvidence],
                                patientLabelId: LongId[PatientLabel],
                                value: FuzzyValue,
                                evidenceText: String,
                                reportId: Option[UuidId[DirectReport]],
                                documentId: Option[LongId[Document]],
                                evidenceId: Option[LongId[ExtractedData]])
