package xyz.driver.pdsuidomain.entities

import java.time.LocalDate

import xyz.driver.entities.assays.AssayType
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.domain.UuidId

object DirectReport {
  implicit def toPhiString(x: DirectReport): PhiString = {
    import x._
    phi"DirectReport(id=$id, patientId=$patientId, reportType=${Unsafe(reportType)}, date=${Unsafe(date)}, " +
      phi"documentType=${Unsafe(documentType)}, providerType=${Unsafe(providerType)}, " +
      phi"providerName=${Unsafe(providerName)})"
  }
}

final case class DirectReport(id: UuidId[DirectReport],
                              patientId: UuidId[Patient],
                              reportType: AssayType,
                              date: LocalDate,
                              documentType: DocumentType,
                              providerType: ProviderType,
                              providerName: String)
