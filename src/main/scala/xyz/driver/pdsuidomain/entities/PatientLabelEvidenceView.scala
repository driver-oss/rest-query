package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._

object PatientLabelEvidenceView {
  implicit def toPhiString(x: PatientLabelEvidenceView): PhiString = {
    import x._
    phi"PatientLabelEvidenceView(id=$id, value=$value, documentId=$documentId, " +
      phi"evidenceId=$evidenceId, reportId=$reportId, patientId=$patientId, labelId=$labelId, " +
      phi"isImplicitMatch=$isImplicitMatch)"
  }
}

final case class PatientLabelEvidenceView(id: LongId[PatientLabelEvidence],
                                          value: FuzzyValue,
                                          evidenceText: String,
                                          documentId: Option[LongId[Document]],
                                          evidenceId: Option[LongId[ExtractedData]],
                                          reportId: Option[UuidId[DirectReport]],
                                          documentType: String,
                                          date: Option[LocalDate], // Document.startDate is optional
                                          providerType: String,
                                          patientId: UuidId[Patient],
                                          labelId: LongId[Label],
                                          isImplicitMatch: Boolean)
