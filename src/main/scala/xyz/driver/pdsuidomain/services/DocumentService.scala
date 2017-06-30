package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object DocumentService {

  trait DefaultNotFoundError {
    def userMessage: String = "Can not find the document"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: Document) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String)(implicit requestContext: AuthenticatedRequestContext)
        extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[Document], totalFound: Int, lastUpdate: Option[LocalDateTime]) extends GetListReply

    type Error = GetListReply with DomainError

    case object AuthorizationError
        extends GetListReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String) extends GetListReply with DomainError
  }

  sealed trait CreateReply
  object CreateReply {
    case class Created(x: Document) extends CreateReply

    type Error = CreateReply with DomainError

    case object NotFoundError extends CreateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends CreateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait UpdateReply
  object UpdateReply {
    case class Updated(updated: Document) extends UpdateReply

    type Error = UpdateReply with DomainError

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String) extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error   => DomainError.toPhiString(x)
    }
  }

  sealed trait DeleteReply
  object DeleteReply {
    case object Deleted extends DeleteReply

    type Error = DeleteReply with DomainError

    case object NotFoundError extends DeleteReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends DeleteReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }

}

trait DocumentService {

  import DocumentService._

  def getById(id: LongId[Document])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def create(draftDocument: Document)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  // Update operations are validated in internal.*Command
  def update(orig: Document, draft: Document)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(id: LongId[Document])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]

  def start(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def submit(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def restart(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def flag(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def resolve(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def unassign(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def archive(orig: Document)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
