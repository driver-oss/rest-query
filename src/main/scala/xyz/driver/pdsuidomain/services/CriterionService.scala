package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object CriterionService {

  trait DefaultNotFoundError {
    def userMessage: String = "Criterion not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  final case class RichCriterion(criterion: Criterion,
                                 armIds: Seq[LongId[EligibilityArm]],
                                 labels: Seq[CriterionLabel])
  object RichCriterion {
    implicit def toPhiString(x: RichCriterion): PhiString = {
      import x._
      phi"RichCriterion(criterion=$criterion, armIds=$armIds, labels=$labels)"
    }
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError

    final case class Created(x: RichCriterion) extends CreateReply

    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    final case class Entity(x: RichCriterion) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String)(implicit requestContext: AuthenticatedRequestContext)
        extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[RichCriterion], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    final case class Updated(updated: RichCriterion) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError
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

trait CriterionService {

  import CriterionService._

  def create(draftRichCriterion: RichCriterion)(
          implicit requestContext: AuthenticatedRequestContext): Future[CreateReply]

  def getById(id: LongId[Criterion])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def update(origRichCriterion: RichCriterion, draftRichCriterion: RichCriterion)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def delete(id: LongId[Criterion])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply]
}
