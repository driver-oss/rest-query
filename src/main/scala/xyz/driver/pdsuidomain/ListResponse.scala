package xyz.driver.pdsuidomain

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.db.Pagination
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData
import xyz.driver.pdsuidomain.services.PatientCriterionService.RichPatientCriterion
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial
import xyz.driver.pdsuidomain.services.PatientHypothesisService.RichPatientHypothesis
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel

@SuppressWarnings(Array("org.wartremover.warts.FinalCaseClass"))
case class ListResponse[+T](items: Seq[T], meta: ListResponse.Meta)

object ListResponse {

  final case class Meta(itemsCount: Int, pageNumber: Int, pageSize: Int, lastUpdate: Option[LocalDateTime])

  object Meta {
    def apply(itemsCount: Int, pagination: Pagination, lastUpdate: Option[LocalDateTime]): Meta = {
      Meta(
        itemsCount,
        pagination.pageNumber,
        pagination.pageSize,
        lastUpdate
      )
    }
  }

  trait MedicalRecordListResponse        extends ListResponse[MedicalRecord]
  trait MedicalRecordIssueListResponse   extends ListResponse[MedicalRecordIssue]
  trait MedicalRecordHistoryListResponse extends ListResponse[MedicalRecordHistory]
  trait DocumentListResponse             extends ListResponse[Document]
  trait DocumentIssueListResponse        extends ListResponse[DocumentIssue]
  trait DocumentHistoryListResponse      extends ListResponse[DocumentHistory]
  trait RichExtractedDataListResponse    extends ListResponse[RichExtractedData]
  trait DocumentTypeListResponse         extends ListResponse[DocumentType]
  trait ProviderTypeListResponse         extends ListResponse[ProviderType]

  trait TrialListResponse                      extends ListResponse[Trial]
  trait TrialIssueListResponse                 extends ListResponse[TrialIssue]
  trait TrialHistoryListResponse               extends ListResponse[TrialHistory]
  trait ArmListResponse                        extends ListResponse[Arm]
  trait InterventionWithArmsListResponse       extends ListResponse[InterventionWithArms]
  trait EligibilityArmWithDiseasesListResponse extends ListResponse[EligibilityArmWithDiseases]
  trait SlotArmListResponse                    extends ListResponse[SlotArm]
  trait RichCriterionListResponse              extends ListResponse[RichCriterion]
  trait InterventionTypeListResponse           extends ListResponse[InterventionType]
  trait StudyDesignListResponse                extends ListResponse[StudyDesign]
  trait HypothesisListResponse                 extends ListResponse[Hypothesis]

  trait PatientListResponse                  extends ListResponse[Patient]
  trait PatientIssueListResponse             extends ListResponse[PatientIssue]
  trait PatientHistoryListResponse           extends ListResponse[PatientHistory]
  trait PatientLabelListResponse             extends ListResponse[PatientLabel]
  trait RichPatientLabelListResponse         extends ListResponse[RichPatientLabel]
  trait RichPatientCriterionListResponse     extends ListResponse[RichPatientCriterion]
  trait RichPatientEligibleTrialListResponse extends ListResponse[RichPatientEligibleTrial]
  trait RichPatientHypothesisListResponse    extends ListResponse[RichPatientHypothesis]
  trait PatientLabelEvidenceViewListResponse extends ListResponse[PatientLabelEvidenceView]

  trait QueueUploadItemListResponse extends ListResponse[BridgeUploadQueue.Item]

}
