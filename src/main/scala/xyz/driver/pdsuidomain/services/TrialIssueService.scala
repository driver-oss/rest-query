package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Trial, TrialIssue}

import scala.concurrent.Future

object TrialIssueService {

  trait DefaultNotFoundError {
    def userMessage: String = "TrialIssue not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    final case class Created(x: TrialIssue) extends CreateReply
    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    final case class Entity(x: TrialIssue)            extends GetByIdReply
    case object NotFoundError                         extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetListByTrialIdReply
  object GetListByTrialIdReply {
    type Error = GetListByTrialIdReply with DomainError
    final case class EntityList(xs: Seq[TrialIssue], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListByTrialIdReply
    case object NotFoundError extends GetListByTrialIdReply with DomainError.NotFoundError with DefaultNotFoundError
    case object AuthorizationError
        extends GetListByTrialIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    final case class Updated(updated: TrialIssue) extends UpdateReply
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

trait TrialIssueService {

  import TrialIssueService._

  def create(draft: TrialIssue)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def getById(trialId: StringId[Trial], id: LongId[TrialIssue])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getListByTrialId(trialId: StringId[Trial],
                       filter: SearchFilterExpr = SearchFilterExpr.Empty,
                       sorting: Option[Sorting] = None,
                       pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListByTrialIdReply]

  def update(orig: TrialIssue, draft: TrialIssue)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(trialId: StringId[Trial], id: LongId[TrialIssue])(
          implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]

}
