package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.ExtractedData

final case class ExportPatientLabelEvidence(id: LongId[ExtractedData],
                                            value: LabelValue,
                                            evidenceText: String,
                                            document: ExportPatientLabelEvidenceDocument)

object ExportPatientLabelEvidence {

  implicit def toPhiString(x: ExportPatientLabelEvidence): PhiString = {
    import x._
    phi"ExportPatientLabelEvidence(id=${Unsafe(id)}, value=${Unsafe(value)}, " +
      phi"evidenceText=${Unsafe(evidenceText)}, document=$document)"
  }

}
