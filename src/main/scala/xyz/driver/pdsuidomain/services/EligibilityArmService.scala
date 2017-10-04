package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{EligibilityArm, EligibilityArmWithDiseases, SlotArm}

import scala.concurrent.Future

object EligibilityArmService {

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  trait DefaultNotFoundError {
    def userMessage: String = "EligibilityArm not found"
  }

  sealed trait GetByIdReply
  object GetByIdReply {

    final case class Entity(x: EligibilityArmWithDiseases) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String)(implicit requestContext: AuthenticatedRequestContext)
        extends GetByIdReply with DomainError
  }

  sealed trait GetListReply
  object GetListReply {
    type Error = GetListReply with DomainError

    final case class EntityList(xs: Seq[EligibilityArmWithDiseases], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {

    final case class Updated(updated: EligibilityArmWithDiseases) extends UpdateReply

    type Error = UpdateReply with DomainError

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError

    final case class AlreadyExistsError(x: EligibilityArmWithDiseases) extends UpdateReply with DomainError {
      val userMessage = s"The arm with such name of trial already exists."
    }

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error   => DomainError.toPhiString(x)
    }
  }

  sealed trait CreateReply
  object CreateReply {
    final case class Created(x: EligibilityArmWithDiseases) extends CreateReply

    type Error = CreateReply with DomainError

    case object AuthorizationError
        extends CreateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends CreateReply with DomainError

    final case class AlreadyExistsError(x: EligibilityArmWithDiseases) extends CreateReply with DomainError {
      val userMessage = s"The arm with this name of trial already exists."
    }

    implicit def toPhiString(reply: CreateReply): PhiString = reply match {
      case Created(x) => phi"Created($x)"
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

    final case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }
}

trait EligibilityArmService {

  import EligibilityArmService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getByEligibilityId(armId: LongId[EligibilityArm])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getBySlotId(armId: LongId[SlotArm])(
    implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def create(slotArmId: LongId[SlotArm], draftEligibilityArm: EligibilityArmWithDiseases)(
          implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def update(origEligibilityArm: EligibilityArmWithDiseases, draftEligibilityArm: EligibilityArmWithDiseases)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(id: LongId[EligibilityArm])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]
}
