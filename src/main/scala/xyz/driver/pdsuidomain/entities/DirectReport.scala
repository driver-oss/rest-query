package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.DirectReport.ReportType

object DirectReport {

  sealed trait ReportType extends Product with Serializable {
    def oneOf(xs: ReportType*): Boolean = xs.contains(this)

    def oneOf(xs: Set[ReportType]): Boolean = xs.contains(this)
  }

  object ReportType {
    case object IHC extends ReportType
    case object DNA extends ReportType
    case object CFDNA extends ReportType

    val All = Set(IHC, DNA, CFDNA)
    implicit def toPhiString(x: ReportType): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: DirectReport): PhiString = {
    import x._
    phi"DirectReport(id=$id, patientId=$patientId, reportType=$reportType, date=${Unsafe(date)}, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, " +
      phi"providerName=${Unsafe(providerName)})"
  }
}

case class DirectReport(id: UuidId[DirectReport],
                        patientId: UuidId[Patient],
                        reportType: ReportType,
                        date: LocalDate,
                        documentType: String,
                        providerType: String,
                        providerName: String)
