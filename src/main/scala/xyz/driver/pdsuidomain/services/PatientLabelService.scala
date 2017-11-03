package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.labels.Label
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object PatientLabelService {

  trait DefaultNotFoundError {
    def userMessage: String = "Patient label not found"
  }

  trait DefaultPatientNotFoundError {
    def userMessage: String = "Patient not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  final case class RichPatientLabel(patientLabel: PatientLabel, isVerified: Boolean)

  object RichPatientLabel {
    implicit def toPhiString(x: RichPatientLabel): PhiString = {
      phi"RichPatientLabel(patientLabel=${x.patientLabel}, isVerified=${x.isVerified})"
    }
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[RichPatientLabel], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetListReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends GetListReply with DomainError
  }

  sealed trait GetDefiningCriteriaListReply
  object GetDefiningCriteriaListReply {
    final case class EntityList(xs: Seq[PatientLabel], totalFound: Int) extends GetDefiningCriteriaListReply

    case object AuthorizationError
        extends GetDefiningCriteriaListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetDefiningCriteriaListReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends GetDefiningCriteriaListReply with DomainError
  }

  sealed trait GetByLabelIdReply
  object GetByLabelIdReply {
    final case class Entity(x: RichPatientLabel) extends GetByLabelIdReply

    type Error = GetByLabelIdReply with DomainError

    case object NotFoundError extends GetByLabelIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends GetByLabelIdReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByLabelIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String) extends GetByLabelIdReply with DomainError

    implicit def toPhiString(reply: GetByLabelIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    final case class Updated(updated: RichPatientLabel) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends UpdateReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error   => DomainError.toPhiString(x)
    }
  }
}

trait PatientLabelService {

  import PatientLabelService._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply]

  def getDefiningCriteriaList(patientId: UuidId[Patient],
                              hypothesisId: UuidId[Hypothesis],
                              pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetDefiningCriteriaListReply]

  def getByLabelIdOfPatient(patientId: UuidId[Patient], labelId: LongId[Label])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByLabelIdReply]

  def update(origPatientLabel: PatientLabel, draftPatientLabel: PatientLabel)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]
}
