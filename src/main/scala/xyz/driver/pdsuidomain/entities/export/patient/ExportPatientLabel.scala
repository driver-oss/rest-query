package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Label, RawPatientLabel}

final case class ExportPatientLabel(id: LongId[Label], evidences: List[ExportPatientLabelEvidence])

object ExportPatientLabel extends PhiLogging {

  implicit def toPhiString(x: ExportPatientLabel): PhiString = {
    import x._
    phi"ExportPatientLabel(id=$id, evidences=$evidences)"
  }

  def fromRaw(labelId: LongId[Label], rawPatientLabels: List[RawPatientLabel]): ExportPatientLabel = {
    ExportPatientLabel(id = labelId, evidences = rawPatientLabels.map(ExportPatientLabelEvidence.fromRaw))
  }
}
