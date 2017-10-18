package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.Sorting
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.Hypothesis

import scala.concurrent.Future

object HypothesisService {
  trait DefaultNotFoundError {
    def userMessage: String = "Intervention not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[Hypothesis], totalFound: Int) extends GetListReply

    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }

  sealed trait CreateReply
  object CreateReply {
    final case class Created(x: Hypothesis) extends CreateReply

    type Error = CreateReply with DomainError

    case object AuthorizationError
        extends CreateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait DeleteReply
  object DeleteReply {
    case object Deleted extends DeleteReply

    type Error = DeleteReply with DomainError

    case object NotFoundError extends DeleteReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends DeleteReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }
}

trait HypothesisService {

  import HypothesisService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def create(draftHypothesis: Hypothesis)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def delete(id: UuidId[Hypothesis])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]
}
