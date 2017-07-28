package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{MedicalRecord, MedicalRecordIssue}

import scala.concurrent.Future

object MedicalRecordIssueService {

  trait DefaultNotFoundError {
    def userMessage: String = "MedicalRecordIssue not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    final case class Created(x: MedicalRecordIssue) extends CreateReply
    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    final case class Entity(x: MedicalRecordIssue)    extends GetByIdReply
    case object NotFoundError                         extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetListByRecordIdReply
  object GetListByRecordIdReply {
    type Error = GetListByRecordIdReply with DomainError
    final case class EntityList(xs: Seq[MedicalRecordIssue], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListByRecordIdReply
    case object NotFoundError extends GetListByRecordIdReply with DomainError.NotFoundError with DefaultNotFoundError
    case object AuthorizationError
        extends GetListByRecordIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    final case class Updated(updated: MedicalRecordIssue) extends UpdateReply
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

trait MedicalRecordIssueService {

  import MedicalRecordIssueService._

  def create(draft: MedicalRecordIssue)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def getById(recordId: LongId[MedicalRecord], id: LongId[MedicalRecordIssue])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getListByRecordId(recordId: LongId[MedicalRecord],
                        filter: SearchFilterExpr = SearchFilterExpr.Empty,
                        sorting: Option[Sorting] = None,
                        pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListByRecordIdReply]

  def update(orig: MedicalRecordIssue, draft: MedicalRecordIssue)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(recordId: LongId[MedicalRecord], id: LongId[MedicalRecordIssue])(
          implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]

}
