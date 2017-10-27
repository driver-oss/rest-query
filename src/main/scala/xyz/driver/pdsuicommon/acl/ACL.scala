package xyz.driver.pdsuicommon.acl

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext

/**
  * @see https://driverinc.atlassian.net/wiki/display/RA/User+permissions#UserPermissions-AccessControlList
  */
object ACL extends PhiLogging {

  import xyz.driver.pdsuicommon.domain.User.Role
  import Role._

  type AclCheck = Role => Boolean

  val Forbid: AclCheck = _ => false

  val Allow: AclCheck = _ => true

  // Common

  object UserHistory
      extends BaseACL(
        label = "user history",
        read = Set(TreatmentMatchingAdmin)
      )

  object Queue
      extends BaseACL(
        label = "queue",
        create = Set(SystemUser),
        read = Set(SystemUser),
        update = Set(SystemUser)
      )

  // REP

  object MedicalRecord
      extends BaseACL(
        label = "medical record",
        read = RepRoles + RoutesCurator + TreatmentMatchingAdmin + ResearchOncologist + SystemUser,
        update = RepRoles - DocumentExtractor
      )

  object MedicalRecordHistory
      extends BaseACL(
        label = "medical record history",
        read = Set(RecordAdmin)
      )

  object MedicalRecordIssue
      extends BaseACL(
        label = "medical record issue",
        create = Set(RecordCleaner, RecordOrganizer, RecordAdmin),
        read = Set(RecordCleaner, RecordOrganizer, RecordAdmin),
        update = Set(RecordCleaner, RecordOrganizer, RecordAdmin),
        delete = Set(RecordCleaner, RecordOrganizer, RecordAdmin)
      )

  object Document
      extends BaseACL(
        label = "document",
        create = Set(RecordOrganizer, RecordAdmin),
        read = RepRoles - RecordCleaner + RoutesCurator + TreatmentMatchingAdmin + ResearchOncologist,
        update = RepRoles - RecordCleaner,
        delete = Set(RecordOrganizer, RecordAdmin)
      )

  object DocumentHistory
      extends BaseACL(
        label = "document history",
        read = Set(RecordAdmin)
      )

  object DocumentIssue
      extends BaseACL(
        label = "document issue",
        create = Set(RecordAdmin, DocumentExtractor),
        read = Set(RecordAdmin, DocumentExtractor),
        update = Set(RecordAdmin, DocumentExtractor),
        delete = Set(RecordAdmin, DocumentExtractor)
      )

  object ExtractedData
      extends BaseACL(
        label = "extracted data",
        create = Set(DocumentExtractor, RecordAdmin),
        read = Set(DocumentExtractor, RecordAdmin, RoutesCurator, TreatmentMatchingAdmin, ResearchOncologist),
        update = Set(DocumentExtractor, RecordAdmin),
        delete = Set(DocumentExtractor, RecordAdmin)
      )

  object ProviderType
      extends BaseACL(
        label = "provider type",
        read = RepRoles + RoutesCurator + TreatmentMatchingAdmin + ResearchOncologist
      )

  object DocumentType
      extends BaseACL(
        label = "document type",
        read = RepRoles + RoutesCurator + TreatmentMatchingAdmin + ResearchOncologist
      )

  object Message
      extends BaseACL(
        label = "message",
        create = RepRoles ++ TreatmentMatchingRoles,
        read = RepRoles ++ TreatmentMatchingRoles,
        update = RepRoles ++ TreatmentMatchingRoles,
        delete = RepRoles ++ TreatmentMatchingRoles
      )

  // TC

  object Trial
      extends BaseACL(
        label = "trial",
        read = TcRoles + RoutesCurator + TreatmentMatchingAdmin + ResearchOncologist + SystemUser,
        update = TcRoles
      )

  object TrialHistory
      extends BaseACL(
        label = "trial history",
        read = Set(TrialAdmin)
      )

  object TrialIssue
      extends BaseACL(
        label = "trial issue",
        create = TcRoles,
        read = TcRoles,
        update = TcRoles,
        delete = TcRoles
      )

  object StudyDesign
      extends BaseACL(
        label = "study design",
        read = Set(TrialSummarizer, TrialAdmin)
      )

  object Hypothesis
      extends BaseACL(
        label = "hypothesis",
        read = Set(TrialSummarizer, TrialAdmin) ++ TreatmentMatchingRoles,
        create = Set(TrialAdmin),
        delete = Set(TrialAdmin)
      )

  object Criterion
      extends BaseACL(
        label = "criterion",
        create = Set(CriteriaCurator, TrialAdmin),
        read = Set(CriteriaCurator, TrialAdmin, RoutesCurator, TreatmentMatchingAdmin, ResearchOncologist),
        update = Set(CriteriaCurator, TrialAdmin),
        delete = Set(CriteriaCurator, TrialAdmin)
      )

  object Arm
      extends BaseACL(
        label = "arm",
        create = Set(TrialSummarizer, TrialAdmin),
        read = TcRoles,
        update = Set(TrialSummarizer, TrialAdmin),
        delete = Set(TrialSummarizer, TrialAdmin)
      )

  object Intervention
      extends BaseACL(
        label = "intervention",
        create = Set(TrialSummarizer, TrialAdmin),
        read = Set(TrialSummarizer, TrialAdmin),
        update = Set(TrialSummarizer, TrialAdmin),
        delete = Set(TrialSummarizer, TrialAdmin)
      )

  object InterventionType
      extends BaseACL(
        label = "intervention type",
        read = Set(TrialSummarizer, TrialAdmin)
      )

  // EV

  object Patient
      extends BaseACL(
        label = "patient",
        read = TreatmentMatchingRoles + ResearchOncologist + SystemUser,
        update = TreatmentMatchingRoles
      )

  object PatientHistory
      extends BaseACL(
        label = "patient history",
        read = Set(TreatmentMatchingAdmin)
      )

  object PatientIssue
      extends BaseACL(
        label = "patient issue",
        create = TreatmentMatchingRoles,
        read = TreatmentMatchingRoles,
        update = TreatmentMatchingRoles,
        delete = TreatmentMatchingRoles
      )

  object PatientLabel
      extends BaseACL(
        label = "patient label",
        read = TreatmentMatchingRoles + ResearchOncologist,
        update = TreatmentMatchingRoles
      )

  object PatientCriterion
      extends BaseACL(
        label = "patient criterion",
        read = TreatmentMatchingRoles + ResearchOncologist,
        update = TreatmentMatchingRoles
      )

  object PatientLabelEvidence
      extends BaseACL(
        label = "patient label evidence",
        read = TreatmentMatchingRoles + ResearchOncologist
      )

  object EligibleTrial
      extends BaseACL(
        label = "eligible trial",
        read = Set(RoutesCurator, TreatmentMatchingAdmin, ResearchOncologist),
        update = Set(RoutesCurator, TreatmentMatchingAdmin)
      )

  object PatientHypothesis
      extends BaseACL(
        label = "patient hypothesis",
        read = Set(RoutesCurator, TreatmentMatchingAdmin),
        update = Set(RoutesCurator, TreatmentMatchingAdmin)
      )

  // Utility code

  abstract class BaseACL(label: String,
                         create: AclCheck = Forbid,
                         read: AclCheck = Forbid,
                         update: AclCheck = Forbid,
                         delete: AclCheck = Forbid) {

    def isCreateAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("create", create)(requestContext.executor.roles)
    }

    def isReadAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("read", read)(requestContext.executor.roles)
    }

    def isUpdateAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("update", update)(requestContext.executor.roles)
    }

    def isDeleteAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("delete", delete)(requestContext.executor.roles)
    }

    private def check(action: String, isAllowed: AclCheck)(executorRoles: Set[Role]): Boolean = {
      loggedError(
        executorRoles.exists(isAllowed),
        phi"${Unsafe(executorRoles.mkString(", "))} has no access to ${Unsafe(action)} a ${Unsafe(label)}"
      )
    }
  }
}
