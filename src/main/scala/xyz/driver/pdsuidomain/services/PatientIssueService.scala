package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Patient, PatientIssue}

import scala.concurrent.Future

object PatientIssueService {

  trait DefaultNotFoundError {
    def userMessage: String = "PatientIssue not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    final case class Created(x: PatientIssue) extends CreateReply
    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    final case class Entity(x: PatientIssue)          extends GetByIdReply
    case object NotFoundError                         extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetListByPatientIdReply
  object GetListByPatientIdReply {
    type Error = GetListByPatientIdReply with DomainError
    final case class EntityList(xs: Seq[PatientIssue], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListByPatientIdReply
    case object NotFoundError extends GetListByPatientIdReply with DomainError.NotFoundError with DefaultNotFoundError
    case object AuthorizationError
        extends GetListByPatientIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    final case class Updated(updated: PatientIssue) extends UpdateReply
    case object AuthorizationError
        extends UpdateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }

  sealed trait DeleteReply
  object DeleteReply {
    type Error = DeleteReply with DomainError
    case object Deleted extends DeleteReply
    case object AuthorizationError
        extends DeleteReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    case object NotFoundError                         extends DeleteReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }
}

trait PatientIssueService {

  import PatientIssueService._

  def create(draft: PatientIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply]

  def getById(patientId: UuidId[Patient], id: LongId[PatientIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply]

  def getListByPatientId(patientId: UuidId[Patient],
                         filter: SearchFilterExpr = SearchFilterExpr.Empty,
                         sorting: Option[Sorting] = None,
                         pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListByPatientIdReply]

  def update(orig: PatientIssue, draft: PatientIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def delete(patientId: UuidId[Patient], id: LongId[PatientIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply]

}
