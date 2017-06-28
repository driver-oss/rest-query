package xyz.driver.pdsuidomain.services


import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.StringId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Trial
import xyz.driver.pdsuidomain.entities.Trial.PdfSource

import scala.concurrent.Future

object TrialService {

  trait DefaultNotFoundError {
    def userMessage: String = "Trial not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[Trial], totalFound: Int, lastUpdate: Option[LocalDateTime])
      extends GetListReply

    case object AuthorizationError
      extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: Trial) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError
      extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
      extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String)(implicit requestContext: AuthenticatedRequestContext)
      extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x) => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait GetPdfSourceReply
  object GetPdfSourceReply {
    type Error = GetPdfSourceReply with DomainError

    case class Entity(x: PdfSource) extends GetPdfSourceReply

    case object AuthorizationError
      extends GetPdfSourceReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object NotFoundError
      extends GetPdfSourceReply with DomainError.NotFoundError {
      def userMessage: String = "Trial's PDF hasn't been found"
    }

    case object TrialNotFoundError
      extends GetPdfSourceReply with DomainError.NotFoundError with DefaultNotFoundError

    case class CommonError(userMessage: String)
      extends GetPdfSourceReply with DomainError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    case class Updated(updated: Trial) extends UpdateReply

    case object NotFoundError
      extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
      extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case class CommonError(userMessage: String)
      extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error => DomainError.toPhiString(x)
    }
  }
}

trait TrialService {

  import TrialService._

  def getById(id: StringId[Trial])
             (implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getPdfSource(trialId: StringId[Trial])
                  (implicit requestContext: AuthenticatedRequestContext): Future[GetPdfSourceReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)
            (implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def update(origTrial: Trial, draftTrial: Trial)
            (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def start(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def submit(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def restart(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def flag(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def resolve(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def archive(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def unassign(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def removeTrialDetails(trialId: StringId[Trial]): Unit
}
