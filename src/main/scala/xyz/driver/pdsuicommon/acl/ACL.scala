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

  object User extends BaseACL(
    label = "user",
    create = Set(RecordAdmin, TrialAdmin, TreatmentMatchingAdmin),
    read = Allow,
    update = Allow,
    delete = Set(RecordAdmin, TrialAdmin, TreatmentMatchingAdmin)
  )

  object Label extends BaseACL(
    label = "label",
    read = RepRoles ++ TcRoles ++ TreatmentMatchingRoles
  )

  // REP

  object MedicalRecord extends BaseACL(
    label = "medical record",
    read = RepRoles + RoutesCurator + TreatmentMatchingAdmin,
    update = RepRoles - DocumentExtractor
  )

  object Document extends BaseACL(
    label = "document",
    create = Set(RecordOrganizer, RecordAdmin),
    read = RepRoles - RecordCleaner + RoutesCurator + TreatmentMatchingAdmin,
    update = RepRoles - RecordCleaner,
    delete = Set(RecordOrganizer, RecordAdmin)
  )

  object ExtractedData extends BaseACL(
    label = "extracted data",
    create = Set(DocumentExtractor, RecordAdmin),
    read = Set(DocumentExtractor, RecordAdmin, RoutesCurator, TreatmentMatchingAdmin),
    update = Set(DocumentExtractor, RecordAdmin),
    delete = Set(DocumentExtractor, RecordAdmin)
  )

  object Keyword extends BaseACL(
    label = "keyword",
    read = Set(DocumentExtractor, RecordAdmin)
  )

  object ProviderType extends BaseACL(
    label = "provider type",
    read = RepRoles + RoutesCurator + TreatmentMatchingAdmin
  )

  object DocumentType extends BaseACL(
    label = "document type",
    read = RepRoles + RoutesCurator + TreatmentMatchingAdmin
  )

  object Message extends BaseACL(
    label = "message",
    create = RepRoles ++ TreatmentMatchingRoles ++ TcRoles,
    read = RepRoles ++ TreatmentMatchingRoles ++ TcRoles,
    update = RepRoles ++ TreatmentMatchingRoles ++ TcRoles,
    delete = RepRoles ++ TreatmentMatchingRoles ++ TcRoles
  )

  // TC

  object Trial extends BaseACL(
    label = "trial",
    read = TcRoles + RoutesCurator + TreatmentMatchingAdmin,
    update = TcRoles
  )

  object StudyDesign extends BaseACL(
    label = "study design",
    read = Set(TrialSummarizer, TrialAdmin)
  )

  object Hypothesis extends BaseACL(
    label = "hypothesis",
    read = Set(TrialSummarizer, TrialAdmin) ++ TreatmentMatchingRoles
  )

  object Criterion extends BaseACL(
    label = "criterion",
    create = Set(CriteriaCurator, TrialAdmin),
    read = Set(CriteriaCurator, TrialAdmin, RoutesCurator, TreatmentMatchingAdmin),
    update = Set(CriteriaCurator, TrialAdmin),
    delete = Set(CriteriaCurator, TrialAdmin)
  )

  object Arm extends BaseACL(
    label = "arm",
    create = Set(TrialSummarizer, TrialAdmin),
    read = TcRoles,
    update = Set(TrialSummarizer, TrialAdmin),
    delete = Set(TrialSummarizer, TrialAdmin)
  )

  object Category extends BaseACL(
    label = "category",
    read = Set(DocumentExtractor, RecordAdmin, CriteriaCurator, TrialAdmin)
  )

  object Intervention extends BaseACL(
    label = "intervention",
    read = Set(TrialSummarizer, TrialAdmin),
    update = Set(TrialSummarizer, TrialAdmin)
  )

  object InterventionType extends BaseACL(
    label = "intervention type",
    read = Set(TrialSummarizer, TrialAdmin)
  )

  // EV

  object Patient extends BaseACL(
    label = "patient",
    read = TreatmentMatchingRoles,
    update = TreatmentMatchingRoles
  )

  object PatientLabel extends BaseACL(
    label = "patient label",
    read = TreatmentMatchingRoles,
    update = TreatmentMatchingRoles
  )

  object PatientCriterion extends BaseACL(
    label = "patient criterion",
    read = TreatmentMatchingRoles,
    update = TreatmentMatchingRoles
  )

  object PatientLabelEvidence extends BaseACL(
    label = "patient label evidence",
    read = TreatmentMatchingRoles
  )

  object EligibleTrial extends BaseACL(
    label = "eligible trial",
    read = Set(RoutesCurator, TreatmentMatchingAdmin),
    update = Set(RoutesCurator, TreatmentMatchingAdmin)
  )

  object PatientHypothesis extends BaseACL(
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
      check("create", create)(requestContext.executor.role)
    }

    def isReadAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("read", read)(requestContext.executor.role)
    }

    def isUpdateAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("update", update)(requestContext.executor.role)
    }

    def isDeleteAllow()(implicit requestContext: AuthenticatedRequestContext): Boolean = {
      check("delete", delete)(requestContext.executor.role)
    }

    private def check(action: String, isAllowed: AclCheck)(executorRole: Role): Boolean = {
      loggedError(
        isAllowed(executorRole),
        phi"$executorRole has no access to ${Unsafe(action)} a ${Unsafe(label)}"
      )
    }

  }

}
