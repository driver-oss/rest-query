package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.error.DomainError

import scala.concurrent.Future

object QueueUploadService {
  trait DefaultNotFoundError {
    def userMessage: String = "Message not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError

    final case class Created(x: BridgeUploadQueue.Item) extends CreateReply
    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError

    final case class Entity(x: BridgeUploadQueue.Item) extends GetByIdReply
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    case object NotFoundError                         extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
  }

  sealed trait GetListReply
  object GetListReply {
    type Error = GetListReply with DomainError

    final case class EntityList(xs: Seq[BridgeUploadQueue.Item], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait ResetReply
  object ResetReply {
    type Error = ResetReply with DomainError

    final case class Updated(updated: BridgeUploadQueue.Item) extends ResetReply
    case object AuthorizationError                            extends ResetReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    case object NotFoundError                                 extends ResetReply with DefaultNotFoundError with DomainError.NotFoundError
    final case class CommonError(userMessage: String)         extends ResetReply with DomainError
  }
}

trait QueueUploadService {

  import QueueUploadService._

  def create(kind: String, tag: String)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def getById(kind: String, tag: String)(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def reset(kind: String, tag: String)(implicit requestContext: AuthenticatedRequestContext): Future[ResetReply]

}
