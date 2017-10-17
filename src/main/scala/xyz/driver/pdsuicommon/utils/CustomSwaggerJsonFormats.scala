package xyz.driver.pdsuicommon.utils

import java.time.{LocalDate, LocalDateTime}

import io.swagger.models.properties.Property
import spray.json.JsValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, TextJson, UuidId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.sprayformats.arm._
import xyz.driver.pdsuidomain.formats.json.sprayformats.slotarm._
import xyz.driver.pdsuidomain.formats.json.sprayformats.eligibilityarm._
import xyz.driver.pdsuidomain.formats.json.sprayformats.criterion._
import xyz.driver.pdsuidomain.formats.json.sprayformats.intervention._
import xyz.driver.pdsuidomain.formats.json.sprayformats.hypothesis._
import xyz.driver.pdsuidomain.formats.json.sprayformats.studydesign._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trial._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialhistory._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialissue._
import xyz.driver.core.swagger.CustomSwaggerJsonConverter._
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuidomain.fakes.entities.common
import xyz.driver.pdsuidomain.formats.json.sprayformats.bridgeuploadqueue._
import xyz.driver.pdsuidomain.formats.json.sprayformats.record._
import xyz.driver.pdsuidomain.formats.json.sprayformats.document._
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData
import xyz.driver.pdsuidomain.services.PatientCriterionService.{DraftPatientCriterion, RichPatientCriterion}
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial
import xyz.driver.pdsuidomain.services.PatientHypothesisService.RichPatientHypothesis
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel

import scala.collection.immutable

object CustomSwaggerJsonFormats {

  val customCommonProperties = immutable.Map[Class[_], Property](
    classOf[LocalDateTime] -> stringProperty(example = Some("2010-12-31'T'18:59:59Z")),
    classOf[LocalDate]     -> stringProperty(example = Some("2010-12-31")),
    classOf[UuidId[_]]     -> stringProperty(example = Some("370b0450-35cb-4aab-ba74-0145be75add5")),
    classOf[StringId[_]]   -> stringProperty(),
    classOf[LongId[_]]     -> stringProperty(),
    classOf[CancerType]    -> stringProperty()
  )

  val customCommonObjectsExamples = immutable.Map[Class[_], JsValue](
    classOf[BridgeUploadQueue.Item] -> queueUploadItemFormat.write(common.nextBridgeUploadQueueItem()),
    classOf[ProviderType]           -> providerTypeFormat.write(common.nextProviderType()),
    classOf[DocumentType]           -> documentTypeFormat.write(common.nextDocumentType())
  )

  val customTrialCurationProperties = immutable.Map[Class[_], Property](
    classOf[Trial.Status]        -> stringProperty(),
    classOf[TrialHistory.Action] -> stringProperty(),
    classOf[TrialHistory.State]  -> stringProperty()
  ) ++ customCommonProperties

  val customTrialCurationObjectsExamples = immutable.Map[Class[_], JsValue](
    classOf[Trial] -> trialWriter.write(xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrial()),
    classOf[Arm]   -> armFormat.write(xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextArm()),
    classOf[TrialHistory] -> trialHistoryFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrialHistory()),
    classOf[TrialIssue] -> trialIssueWriter.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextTrialIssue()),
    classOf[RichCriterion] -> richCriterionFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextRichCriterion()),
    classOf[InterventionWithArms] -> interventionFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextInterventionWithArms()),
    classOf[InterventionType] -> interventionTypeFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextInterventionType()),
    classOf[Hypothesis] -> hypothesisFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextHypothesis()),
    classOf[StudyDesign] -> studyDesignFormat.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextStudyDesign()),
    classOf[EligibilityArmWithDiseases] -> eligibilityArmWithDiseasesWriter.write(
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextEligibilityArmWithDiseases()),
    classOf[SlotArm] -> slotArmFormat.write(xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextSlotArm())
  )

  // records-processing-service
  object Rep {
    import xyz.driver.pdsuidomain.fakes.entities.recordprocessing._
    import xyz.driver.pdsuidomain.formats.json.sprayformats._

    val customRecordProcessingProperties = immutable.Map[Class[_], Property](
      classOf[MedicalRecord.Status]        -> stringProperty(),
      classOf[MedicalRecordHistory.Action] -> stringProperty(),
      classOf[MedicalRecordHistory.State]  -> stringProperty(),
      classOf[Document.Status]             -> stringProperty(),
      classOf[Document.RequiredType]       -> stringProperty(),
      classOf[DocumentHistory.Action]      -> stringProperty(),
      classOf[DocumentHistory.State]       -> stringProperty()
    ) ++ customCommonProperties

    val customRepObjectsExamples = immutable.Map[Class[_], JsValue](
      classOf[Document] ->
        document.documentFormat.write(nextDocument()),
      classOf[DocumentIssue] ->
        documentissue.documentIssueFormat.write(nextDocumentIssue()),
      classOf[DocumentHistory] ->
        documenthistory.documentHistoryFormat.write(nextDocumentHistory()),
      classOf[TextJson[List[MedicalRecord.Meta]]] ->
        record.recordMetaFormat.write(nextMedicalRecordMetaJson()),
      classOf[MedicalRecord] ->
        record.recordFormat.write(nextMedicalRecord()),
      classOf[MedicalRecordIssue] ->
        recordissue.recordIssueFormat.write(nextMedicalRecordIssue()),
      classOf[MedicalRecordHistory] ->
        recordhistory.recordHistoryFormat.write(nextMedicalRecordHistory()),
      classOf[RichExtractedData] ->
        extracteddata.extractedDataFormat.write(nextRichExtractedData()),
      classOf[MedicalRecord.Meta] ->
        record.recordMetaTypeFormat.write(nextMedicalRecordMeta()),
      classOf[TextJson[Document.Meta]] ->
        document.fullDocumentMetaFormat.write(nextDocumentMetaJson()),
      classOf[ExtractedData.Meta] ->
        extracteddata.extractedDataMetaFormat.write(nextExtractedDataMeta()),
      classOf[ExtractedData.Meta.Evidence] ->
        extracteddata.metaEvidenceFormat.write(nextExtractedDataMetaEvidence()),
      classOf[ExtractedData.Meta.Keyword] ->
        extracteddata.metaKeywordFormat.write(nextExtractedDataMetaKeyword()),
      classOf[ExtractedData.Meta.TextLayerPosition] ->
        extracteddata.metaTextLayerPositionFormat.write(nextExtractedDataMetaTextLayerPosition()),
      classOf[TextJson[ExtractedData.Meta]] ->
        extracteddata.fullExtractedDataMetaFormat.write(nextExtractedDataMetaJson())
    ) ++ customCommonObjectsExamples
  }

  object treatmentmatching {
    import xyz.driver.pdsuidomain.fakes.entities.treatmentmatching._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patient._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patientcriterion._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patientdefiningcriteria._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patienteligibletrial._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patientlabel._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patienthypothesis._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patienthistory._
    import xyz.driver.pdsuidomain.formats.json.sprayformats.patientissue._

    val customTreatmentMatchingProperties = immutable.Map[Class[_], Property](
      classOf[Patient.Status]        -> stringProperty(),
      classOf[PatientHistory.Action] -> stringProperty(),
      classOf[PatientHistory.State]  -> stringProperty()
    ) ++ customCommonProperties

    val customTreatmentMatchingObjectsExamples = immutable.Map[Class[_], JsValue](
      classOf[Patient]                  -> patientWriter.write(nextPatient()),
      classOf[RichPatientLabel]         -> richPatientLabelWriter.write(nextRichPatientLabel()),
      classOf[PatientLabel]             -> patientLabelDefiningCriteriaWriter.write(nextPatientLabel()),
      classOf[RichPatientCriterion]     -> patientCriterionWriter.write(nextRichPatientCriterion()),
      classOf[DraftPatientCriterion]    -> draftPatientCriterionFormat.write(nextDraftPatientCriterion()),
      classOf[PatientLabelEvidenceView] -> patientLabelEvidenceWriter.write(nextPatientLabelEvidenceView()),
      classOf[RichPatientEligibleTrial] -> patientEligibleTrialWriter.write(nextRichPatientEligibleTrial()),
      classOf[PatientHypothesis]        -> patientHypothesisWriter.write(nextPatientHypothesis()),
      classOf[RichPatientHypothesis]    -> richPatientHypothesisWriter.write(nextRichPatientHypothesis()),
      classOf[PatientHistory]           -> patientHistoryFormat.write(nextPatientHistory()),
      classOf[PatientIssue]             -> patientIssueWriter.write(nextPatientIssue())
    ) ++ customCommonObjectsExamples
  }

}
