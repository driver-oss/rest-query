package xyz.driver.pdsuidomain.entities

import xyz.driver.core.Id
import xyz.driver.core.date.Date
import xyz.driver.entities.assays.AssayType
import xyz.driver.entities.clinic.{ClinicalRecord, TestOrder}
import xyz.driver.entities.common.FullName
import xyz.driver.entities.labels.{Label, LabelValue}
import xyz.driver.entities.patient.CancerType
import xyz.driver.entities.process.ProcessStepExecutionStatus
import xyz.driver.entities.report.Report
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrialWithLabels

object eligibility {

  sealed trait EvidenceDocument {
    val documentType: DocumentType
    val providerType: ProviderType
    val providerName: Option[String]
    val date: Option[Date]
    val isDriverDocument: Boolean
  }

  final case class MolecularEvidenceDocument(documentType: DocumentType,
                                             providerType: ProviderType,
                                             providerName: Option[String],
                                             date: Option[Date],
                                             reportId: Id[Report],
                                             reportType: AssayType,
                                             isDriverDocument: Boolean = true)
      extends EvidenceDocument

  final case class ClinicalEvidenceDocument(documentId: Id[ClinicalEvidenceDocument],
                                            documentType: DocumentType,
                                            providerType: ProviderType,
                                            providerName: Option[String],
                                            date: Option[Date],
                                            requestId: Id[ClinicalRecord],
                                            isDriverDocument: Boolean = false)
      extends EvidenceDocument

  // Some fields are optional because they are not in the backend response
  final case class Evidence(evidenceId: Option[Id[Evidence]],
                            evidenceText: String,
                            labelValue: LabelValue,
                            document: EvidenceDocument,
                            isPrimaryValue: Option[Boolean] = None)

  final case class LabelWithEvidence(label: Label, evidence: Seq[Evidence] = Seq.empty)

  final case class LabelMismatchRank(label: Label,
                                     score: Int,
                                     trials: Seq[ExportTrialWithLabels],
                                     evidence: Seq[Evidence])
  final case class MismatchRankedLabels(data: Seq[LabelMismatchRank], labelVersion: Int)

  final case class MatchedPatient(patientId: Id[MatchedPatient],
                                  name: FullName[MatchedPatient],
                                  birthDate: Date,
                                  orderId: Id[TestOrder],
                                  disease: CancerType,
                                  patientDataStatus: ProcessStepExecutionStatus)
}
