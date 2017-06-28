package xyz.driver.pdsuidomain.services


import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Arm

import scala.concurrent.Future

object ArmService {

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  trait DefaultNotFoundError {
    def userMessage: String = "Arm not found"
  }

  sealed trait GetByIdReply
  object GetByIdReply {

    case class Entity(x: Arm) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError
      extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
      extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String)(implicit requestContext: AuthenticatedRequestContext)
      extends GetByIdReply with DomainError

  }

  sealed trait GetListReply
  object GetListReply {
    type Error = GetListReply with DomainError

    case class EntityList(xs: Seq[Arm], totalFound: Int) extends GetListReply

    case object AuthorizationError
      extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

  }

  sealed trait UpdateReply
  object UpdateReply {

    case class Updated(updated: Arm) extends UpdateReply

    type Error = UpdateReply with DomainError

    case object NotFoundError
      extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
      extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String)
      extends UpdateReply with DomainError

    case class AlreadyExistsError(x: Arm) extends UpdateReply with DomainError {
      val userMessage = s"The arm with such name of trial already exists."
    }

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error => DomainError.toPhiString(x)
    }
  }

  sealed trait CreateReply
  object CreateReply {
    case class Created(x: Arm) extends CreateReply

    type Error = CreateReply with DomainError

    case object AuthorizationError
      extends CreateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String)
      extends CreateReply with DomainError

    case class AlreadyExistsError(x: Arm) extends CreateReply with DomainError {
      val userMessage = s"The arm with this name of trial already exists."
    }

    implicit def toPhiString(reply: CreateReply): PhiString = reply match {
      case Created(x) => phi"Created($x)"
      case x: Error => DomainError.toPhiString(x)
    }
  }

  sealed trait DeleteReply
  object DeleteReply {
    case object Deleted extends DeleteReply

    type Error = DeleteReply with DomainError

    case object NotFoundError
      extends DeleteReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
      extends DeleteReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String)
      extends DeleteReply with DomainError
  }
}

trait ArmService {

  import ArmService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)
            (implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getById(armId: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def create(draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def update(origArm: Arm, draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(id: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]
}
