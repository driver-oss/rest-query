package xyz.driver.pdsuidomain.fakes.entities

import xyz.driver.core.generators
import xyz.driver.entities.clinic.{ClinicalRecord, TestOrder}
import xyz.driver.entities.patient.{CancerType, Patient}
import xyz.driver.entities.report.Report
import xyz.driver.fakes
import xyz.driver.pdsuidomain.entities.eligibility._

object eligibility {
  import xyz.driver.core.generators._

  def nextMolecularEvidenceDocument(): MolecularEvidenceDocument =
    MolecularEvidenceDocument(
      documentType = xyz.driver.pdsuidomain.fakes.entities.rep.DocumentGen.nextDocumentType(),
      providerType = xyz.driver.pdsuidomain.fakes.entities.rep.MedicalRecordGen.nextProviderType(),
      providerName = nextOption(nextString(100)),
      date = nextOption(nextDate()),
      reportId = nextId[Report](),
      reportType = fakes.entities.assays.nextAssayType(),
      isDriverDocument = nextBoolean()
    )

  def nextClinicalEvidenceDocument(): ClinicalEvidenceDocument =
    ClinicalEvidenceDocument(
      documentType = xyz.driver.pdsuidomain.fakes.entities.rep.DocumentGen.nextDocumentType(),
      providerType = xyz.driver.pdsuidomain.fakes.entities.rep.MedicalRecordGen.nextProviderType(),
      providerName = nextOption(nextString(100)),
      date = nextOption(nextDate()),
      documentId = nextId[ClinicalEvidenceDocument](),
      requestId = nextId[ClinicalRecord](),
      isDriverDocument = nextBoolean()
    )

  def nextEvidenceDocument(): EvidenceDocument =
    oneOf[EvidenceDocument](nextMolecularEvidenceDocument(),
                            nextClinicalEvidenceDocument(),
                            nextClinicalEvidenceDocument()) // For more clinical documents

  def nextEvidence(): Evidence =
    Evidence(
      evidenceId = Option(nextId[Evidence]()),
      evidenceText = nextString(100),
      labelValue = xyz.driver.fakes.entities.labels.nextLabelValue(),
      nextEvidenceDocument(),
      isPrimaryValue = nextOption(nextBoolean())
    )

  def nextLabelEvidence(): LabelEvidence =
    LabelEvidence(label = fakes.entities.labels.nextLabel(), evidence = Seq.empty)

  def nextLabelMismatchRank(): LabelMismatchRank =
    LabelMismatchRank(
      label = fakes.entities.labels.nextLabel(),
      score = nextInt(100),
      trials = seqOf(xyz.driver.pdsuidomain.fakes.entities.export.nextExportTrialWithLabels()),
      evidence = seqOf(nextEvidence())
    )

  def nextMismatchRankedLabels(): MismatchRankedLabels =
    MismatchRankedLabels(data = seqOf(nextLabelMismatchRank()), labelVersion = nextInt(100))

  def nextMatchedPatient(): MatchedPatient =
    MatchedPatient(
      patientId = nextId[Patient](),
      name = fakes.entities.common.nextFullName[Patient](),
      birthDate = nextDate(),
      orderId = nextId[TestOrder](),
      disease = generators.oneOf[CancerType](CancerType.Breast, CancerType.Lung, CancerType.Prostate),
      patientDataStatus = fakes.entities.process.nextProcessStepExecutionStatus()
    )
}
