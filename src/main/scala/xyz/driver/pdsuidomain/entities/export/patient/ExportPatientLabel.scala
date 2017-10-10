package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

final case class ExportPatientLabel(id: LongId[Label], evidences: List[ExportPatientLabelEvidence])

object ExportPatientLabel extends PhiLogging {

  implicit def toPhiString(x: ExportPatientLabel): PhiString = {
    import x._
    phi"ExportPatientLabel(id=$id, evidences=$evidences)"
  }
}
