package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Document, DocumentIssue}

import scala.concurrent.Future

object DocumentIssueService {

  trait DefaultNotFoundError {
    def userMessage: String = "DocumentIssue not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    final case class Created(x: DocumentIssue) extends CreateReply
    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    final case class Entity(x: DocumentIssue)         extends GetByIdReply
    case object NotFoundError                         extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetListByDocumentIdReply
  object GetListByDocumentIdReply {
    type Error = GetListByDocumentIdReply with DomainError
    final case class EntityList(xs: Seq[DocumentIssue], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListByDocumentIdReply
    case object NotFoundError extends GetListByDocumentIdReply with DomainError.NotFoundError with DefaultNotFoundError
    case object AuthorizationError
        extends GetListByDocumentIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    final case class Updated(updated: DocumentIssue) extends UpdateReply
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

trait DocumentIssueService {

  import DocumentIssueService._

  def create(draft: DocumentIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply]

  def getById(documentId: LongId[Document], id: LongId[DocumentIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply]

  def getListByDocumentId(documentId: LongId[Document],
                          filter: SearchFilterExpr = SearchFilterExpr.Empty,
                          sorting: Option[Sorting] = None,
                          pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListByDocumentIdReply]

  def update(orig: DocumentIssue, draft: DocumentIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def delete(documentId: LongId[Document], id: LongId[DocumentIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply]

}
