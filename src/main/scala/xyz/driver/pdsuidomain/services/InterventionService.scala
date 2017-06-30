package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object InterventionService {

  trait DefaultNotFoundError {
    def userMessage: String = "Intervention not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[InterventionWithArms], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: InterventionWithArms) extends GetByIdReply

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

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    case class Updated(updated: InterventionWithArms) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }

}

trait InterventionService {

  import InterventionService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getById(id: LongId[Intervention])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def update(origIntervention: InterventionWithArms, draftIntervention: InterventionWithArms)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
