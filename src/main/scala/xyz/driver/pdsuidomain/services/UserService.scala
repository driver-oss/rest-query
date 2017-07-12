package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.{AnonymousRequestContext, AuthenticatedRequestContext}
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError

import scala.concurrent.Future

object UserService {

  trait DefaultCredentialsError {
    def userMessage: String = "Incorrect email/password. Try again."
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  trait DefaultNotFoundError {
    def userMessage: String = "User not found"
  }

  sealed trait ActivateExecutorReply
  object ActivateExecutorReply {
    type Error = ActivateExecutorReply with DomainError
    case class Entity(x: User) extends ActivateExecutorReply
    case object NotFoundError extends ActivateExecutorReply with DomainError.NotFoundError {
      val userMessage = "Info about you is not found on the server"
    }
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    case class Entity(x: User) extends GetByIdReply
    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
    case object NotFoundError                   extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError
    case class CommonError(userMessage: String) extends GetByIdReply with DomainError
  }

  sealed trait GetByEmailReply
  object GetByEmailReply {
    case class Entity(x: User) extends GetByEmailReply
    case object NotFoundError extends GetByEmailReply with DefaultNotFoundError with DomainError.NotFoundError {
      override def userMessage: String = "Incorrect email. Try again."
    }
  }

  sealed trait GetByCredentialsReply
  object GetByCredentialsReply {
    case class Entity(x: User) extends GetByCredentialsReply
    case object AuthenticationError
        extends GetByCredentialsReply with DefaultCredentialsError with DomainError.AuthenticationError
    case object NotFoundError extends GetByCredentialsReply with DomainError.NotFoundError with DefaultNotFoundError
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[User], totalFound: Int) extends GetListReply
    case object AuthorizationError                        extends GetListReply with DomainError.AuthorizationError with DefaultNotFoundError
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    case class Created(x: User)    extends CreateReply
    case object AuthorizationError extends CreateReply with DefaultNotFoundError with DomainError.AuthorizationError
    case class UserAlreadyExistsError(email: Email) extends CreateReply with DomainError {
      val userMessage = s"The user with this email already exists."
    }
    case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    case class Updated(updated: User) extends UpdateReply
    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError
    case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }

  sealed trait DeleteReply
  object DeleteReply {
    type Error = DeleteReply with DomainError
    case object Deleted extends DeleteReply
    case class AuthorizationError(user: User)
        extends DeleteReply with DefaultAccessDeniedError with DomainError.AuthorizationError
    case object AssignedToRecordAndDocumentError extends DeleteReply with DomainError {
      val userMessage = "User is can not be deleted because he has record and document in work"
    }
    case object NotFoundError                   extends DeleteReply with DefaultNotFoundError with DomainError.NotFoundError
    case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }
}

trait UserService {

  import UserService._

  /**
    * Utility method for getting request executor.
    */
  def activateExecutor(executorId: StringId[User])(
          implicit requestContext: AnonymousRequestContext): Future[ActivateExecutorReply]

  def getById(userId: StringId[User])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getByEmail(email: Email)(implicit requestContext: AnonymousRequestContext): Future[GetByEmailReply]

  def getByCredentials(email: Email, password: String)(
          implicit requestContext: AnonymousRequestContext): Future[GetByCredentialsReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def create(draftUser: User)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def update(origUser: User, draftUser: User)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(userId: StringId[User])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]
}
