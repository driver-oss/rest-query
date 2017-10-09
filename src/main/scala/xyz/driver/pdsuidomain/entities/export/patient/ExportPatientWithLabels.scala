package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Patient

final case class ExportPatientWithLabels(patientId: UuidId[Patient],
                                         labelVersion: Long,
                                         labels: List[ExportPatientLabel])

object ExportPatientWithLabels {

  implicit def toPhiString(x: ExportPatientWithLabels): PhiString = {
    import x._
    phi"ExportPatientWithLabels(patientId=$patientId, version=${Unsafe(labelVersion)}, labels=$labels)"
  }
}
