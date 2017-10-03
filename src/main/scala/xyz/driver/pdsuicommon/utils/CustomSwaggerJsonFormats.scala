package xyz.driver.pdsuicommon.utils

import java.time.{LocalDate, LocalDateTime}

import io.swagger.models.properties.Property
import spray.json.JsValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, TextJson, UuidId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.sprayformats.arm._
import xyz.driver.pdsuidomain.formats.json.sprayformats.criterion._
import xyz.driver.pdsuidomain.formats.json.sprayformats.intervention._
import xyz.driver.pdsuidomain.formats.json.sprayformats.hypothesis._
import xyz.driver.pdsuidomain.formats.json.sprayformats.studydesign._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trial._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialhistory._
import xyz.driver.pdsuidomain.formats.json.sprayformats.trialissue._
import xyz.driver.core.swagger.CustomSwaggerJsonConverter._
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

import scala.collection.immutable

object CustomSwaggerJsonFormats {

  val customCommonProperties = immutable.Map[Class[_], Property](
    classOf[LocalDateTime] -> stringProperty(example = Some("2010-12-31'T'18:59:59Z")),
    classOf[LocalDate]     -> stringProperty(example = Some("2010-12-31")),
    classOf[UuidId[_]]     -> stringProperty(example = Some("370b0450-35cb-4aab-ba74-0145be75add5")),
    classOf[StringId[_]]   -> stringProperty(),
    classOf[LongId[_]]     -> stringProperty()
  )
  val customTrialCurationProperties = immutable.Map[Class[_], Property](
    classOf[Trial.Status]        -> stringProperty(),
    classOf[Trial.Condition]     -> stringProperty(),
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
      xyz.driver.pdsuidomain.fakes.entities.trialcuration.nextStudyDesign())
  )

  // records-processing-service
  object Rep {
    import xyz.driver.pdsuidomain.fakes.entities.rep
    import xyz.driver.pdsuidomain.formats.json.sprayformats.document
    import xyz.driver.pdsuidomain.formats.json.sprayformats.documentissue
    import xyz.driver.pdsuidomain.formats.json.sprayformats.documenthistory
    import xyz.driver.pdsuidomain.formats.json.sprayformats.record
    import xyz.driver.pdsuidomain.formats.json.sprayformats.recordissue
    import xyz.driver.pdsuidomain.formats.json.sprayformats.recordhistory
    import xyz.driver.pdsuidomain.formats.json.sprayformats.bridgeuploadqueue
    import xyz.driver.pdsuidomain.formats.json.sprayformats.extracteddata

    val customRepObjectsExamples = immutable.Map[Class[_], JsValue](
      classOf[Document] ->
        document.documentFormat.write(rep.DocumentGen.nextDocument()),
      classOf[Document.Meta] ->
        document.documentMetaFormat.write(rep.DocumentGen.nextDocumentMeta()),
      classOf[TextJson[Document.Meta]] ->
        document.fullDocumentMetaFormat.write(rep.DocumentGen.nextDocumentMetaJson()),
      classOf[Document.RequiredType] ->
        document.requiredTypeFormat.write(rep.DocumentGen.nextDocumentRequiredType()),
      classOf[Document.Status] ->
        document.documentStatusFormat.write(rep.DocumentGen.nextDocumentStatus()),
      classOf[DocumentIssue] ->
        documentissue.documentIssueFormat.write(rep.DocumentGen.nextDocumentIssue()),
      classOf[DocumentHistory] ->
        documenthistory.documentHistoryFormat.write(rep.DocumentGen.nextDocumentHistory()),
      classOf[DocumentHistory.Action] ->
        documenthistory.documentActionFormat.write(rep.DocumentGen.nextDocumentHistoryAction()),
      classOf[DocumentHistory.State] ->
        documenthistory.documentStateFormat.write(rep.DocumentGen.nextDocumentHistoryState()),
      classOf[ProviderType] ->
        record.providerTypeFormat.write(rep.MedicalRecordGen.nextProviderType()),
      classOf[TextJson[List[MedicalRecord.Meta]]] ->
        record.recordMetaFormat.write(rep.MedicalRecordGen.nextMedicalRecordMetasJson()),
      classOf[MedicalRecord] ->
        record.recordFormat.write(rep.MedicalRecordGen.nextMedicalRecord()),
      classOf[MedicalRecord.Meta] ->
        record.recordMetaTypeFormat.write(rep.MedicalRecordGen.nextMedicalRecordMeta()),
      classOf[MedicalRecord.Status] ->
        record.recordStatusFormat.write(rep.MedicalRecordGen.nextMedicalRecordStatus()),
      classOf[MedicalRecordIssue] ->
        recordissue.recordIssueFormat.write(rep.MedicalRecordGen.nextMedicalRecordIssue()),
      classOf[MedicalRecordHistory] ->
        recordhistory.recordHistoryFormat.write(rep.MedicalRecordGen.nextMedicalRecordHistory()),
      classOf[MedicalRecordHistory.Action] ->
        recordhistory.recordActionFormat.write(rep.MedicalRecordGen.nextMedicalRecordHistoryAction()),
      classOf[MedicalRecordHistory.State] ->
        recordhistory.recordStateFormat.write(rep.MedicalRecordGen.nextMedicalRecordHistoryState()),
      classOf[BridgeUploadQueue.Item] ->
        bridgeuploadqueue.queueUploadItemFormat.write(rep.BridgeUploadQueueGen.nextBridgeUploadQueueItem()),
      classOf[ExtractedData.Meta] ->
        extracteddata.extractedDataMetaFormat.write(rep.ExtractedDataGen.nextExtractedDataMeta()),
      classOf[ExtractedData.Meta.Evidence] ->
        extracteddata.metaEvidenceFormat.write(rep.ExtractedDataGen.nextExtractedDataMetaEvidence()),
      classOf[ExtractedData.Meta.Keyword] ->
        extracteddata.metaKeywordFormat.write(rep.ExtractedDataGen.nextExtractedDataMetaKeyword()),
      classOf[ExtractedData.Meta.TextLayerPosition] ->
        extracteddata.metaTextLayerPositionFormat.write(rep.ExtractedDataGen.nextExtractedDataMetaTextLayerPosition()),
      classOf[TextJson[ExtractedData.Meta]] ->
        extracteddata.fullExtractedDataMetaFormat.write(rep.ExtractedDataGen.nextExtractedDataMetaJson()),
      classOf[RichExtractedData] ->
        extracteddata.extractedDataFormat.write(rep.ExtractedDataGen.nextRichExtractedData()),
      classOf[ExtractedDataLabel] ->
        extracteddata.extractedDataLabelWriter.write(rep.ExtractedDataGen.nextExtractedDataLabel())
    )
  }

}
