package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{ExtractedData, RawPatientLabel}

case class ExportPatientLabelEvidence(id: LongId[ExtractedData],
                                      value: FuzzyValue,
                                      evidenceText: String,
                                      document: ExportPatientLabelEvidenceDocument)

object ExportPatientLabelEvidence {

  implicit def toPhiString(x: ExportPatientLabelEvidence): PhiString = {
    import x._
    phi"ExportPatientLabelEvidence(id=${Unsafe(id)}, value=$value, " +
      phi"evidenceText=${Unsafe(evidenceText)}, document=$document)"
  }

  def fromRaw(x: RawPatientLabel) = ExportPatientLabelEvidence(
    id = x.evidenceId,
    value = x.value,
    evidenceText = x.evidenceText,
    document = ExportPatientLabelEvidenceDocument(
      x.documentId,
      x.requestId,
      x.documentType,
      x.providerType,
      x.startDate
    )
  )
}
