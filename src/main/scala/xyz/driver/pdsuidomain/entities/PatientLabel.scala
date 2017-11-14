package xyz.driver.pdsuidomain.entities

import xyz.driver.entities.labels.{Label, LabelValue}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
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
                              primaryValue: LabelValue,
                              verifiedPrimaryValue: LabelValue,
                              isImplicitMatch: Boolean,
                              isVisible: Boolean)

object PatientLabelEvidence {
  implicit def toPhiString(x: PatientLabelEvidence): PhiString = {
    import x._
    phi"PatientLabelEvidence(id=$id, patientLabelId=$patientLabelId, value=${Unsafe(value)}, " +
      phi"documentId=$documentId, evidenceId=$evidenceId, reportId=$reportId)"
  }
}

final case class PatientLabelEvidence(id: LongId[PatientLabelEvidence],
                                      patientLabelId: LongId[PatientLabel],
                                      value: LabelValue,
                                      evidenceText: String,
                                      reportId: Option[UuidId[DirectReport]],
                                      documentId: Option[LongId[Document]],
                                      evidenceId: Option[LongId[ExtractedData]])
