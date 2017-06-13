package xyz.driver.pdsuidomain.entities.export.patient

import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Patient, RawPatientLabel}

import scala.collection.breakOut

case class ExportPatientWithLabels(patientId: UuidId[Patient], labelVersion: Long, labels: List[ExportPatientLabel])

object ExportPatientWithLabels {

  implicit def toPhiString(x: ExportPatientWithLabels): PhiString = {
    import x._
    phi"ExportPatientWithLabels(patientId=$patientId, version=${Unsafe(labelVersion)}, labels=$labels)"
  }

  def fromRaw(patientId: UuidId[Patient], rawPatientRefs: List[RawPatientLabel]) = ExportPatientWithLabels(
    patientId = patientId,
    labelVersion = 1L, // TODO It is needed to replace this mock label version.
    labels = rawPatientRefs
      .groupBy(_.labelId)
      .map(Function.tupled(ExportPatientLabel.fromRaw))(breakOut)
  )
}
