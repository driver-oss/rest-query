package xyz.driver.pdsuidomain.fakes.entities

import xyz.driver.core.generators
import xyz.driver.entities.clinic.ClinicalRecord
import xyz.driver.entities.labels.Label
import xyz.driver.fakes
import xyz.driver.pdsuidomain.entities.export.patient._
import xyz.driver.pdsuidomain.entities.export.trial._
import xyz.driver.pdsuidomain.entities._

object export {
  import common._
  import xyz.driver.core.generators._

  def nextExportTrialArm(): ExportTrialArm =
    ExportTrialArm(armId = nextLongId[EligibilityArm],
                   armName = nextString(100),
                   diseaseList = listOf(nextString(100)))

  def nextExportTrialLabelCriterion(): ExportTrialLabelCriterion =
    ExportTrialLabelCriterion(
      criterionId = nextLongId[Criterion],
      value = nextOption[Boolean](nextBoolean()),
      labelId = nextLongId[Label],
      armIds = setOf(nextLongId[EligibilityArm]),
      criteria = nextString(100),
      isCompound = nextBoolean(),
      isDefining = nextBoolean(),
      inclusion = nextOption(nextBoolean())
    )

  def nextExportTrialWithLabels(): ExportTrialWithLabels =
    ExportTrialWithLabels(
      nctId = nextStringId[Trial],
      trialId = nextUuidId[Trial],
      lastReviewed = nextLocalDateTime,
      labelVersion = nextInt(100).toLong,
      arms = listOf(nextExportTrialArm()),
      criteria = listOf(nextExportTrialLabelCriterion())
    )

  def nextExportPatientLabelEvidenceDocument(): ExportPatientLabelEvidenceDocument = {
    ExportPatientLabelEvidenceDocument(
      documentId = nextLongId[Document],
      requestId = generators.nextId[ClinicalRecord](),
      documentType = nextDocumentType(),
      providerType = nextProviderType(),
      date = nextLocalDate
    )
  }

  def nextExportPatientLabelEvidence(): ExportPatientLabelEvidence = {
    ExportPatientLabelEvidence(
      id = nextLongId[ExtractedData],
      value = fakes.entities.labels.nextLabelValue(),
      evidenceText = nextString(),
      document = nextExportPatientLabelEvidenceDocument()
    )
  }

  def nextExportPatientLabel(): ExportPatientLabel = {
    ExportPatientLabel(
      id = nextLongId[Label],
      evidences = List.fill(nextInt(10))(nextExportPatientLabelEvidence())
    )
  }

  def nextExportPatientWithLabels(): ExportPatientWithLabels = {
    ExportPatientWithLabels(
      patientId = nextUuidId[Patient],
      labelVersion = nextInt(Int.MaxValue).toLong,
      labels = List.fill(nextInt(10))(nextExportPatientLabel())
    )
  }

}
