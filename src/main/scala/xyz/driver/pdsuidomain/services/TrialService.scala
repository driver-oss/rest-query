package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.patient.CancerType
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.StringId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Trial, TrialCreationRequest}
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrialWithLabels

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
    final case class EntityList(xs: Seq[Trial], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    final case class Entity(x: Trial) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String)(
            implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo])
        extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait GetTrialWithLabelsReply
  object GetTrialWithLabelsReply {
    type Error = GetTrialWithLabelsReply with DomainError

    final case class Entity(x: ExportTrialWithLabels) extends GetTrialWithLabelsReply

    case object NotFoundError extends GetTrialWithLabelsReply with DomainError.NotFoundError {
      def userMessage: String = "Trial not found"
    }

    case object AuthorizationError
        extends GetTrialWithLabelsReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetTrialsWithLabelsReply
  object GetTrialsWithLabelsReply {
    type Error = GetTrialsWithLabelsReply with DomainError

    final case class EntityList(xs: Seq[ExportTrialWithLabels]) extends GetTrialsWithLabelsReply

    case object NotFoundError extends GetTrialsWithLabelsReply with DomainError.NotFoundError {
      def userMessage: String = "Trials for disease are not found"
    }

    case object AuthorizationError
        extends GetTrialsWithLabelsReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    final case class Updated(updated: Trial) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error   => DomainError.toPhiString(x)
    }
  }
}

trait TrialService {

  import TrialService._

  def getById(id: StringId[Trial])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply]

  def getTrialWithLabels(trialId: StringId[Trial], cancerType: CancerType)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetTrialWithLabelsReply]

  def getTrialsWithLabels(cancerType: CancerType)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetTrialsWithLabelsReply]

  def getPdfSource(trialId: StringId[Trial])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Source[ByteString, NotUsed]]

  def getHtmlSource(trialId: StringId[Trial])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Source[ByteString, NotUsed]]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply]

  def update(origTrial: Trial, draftTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def start(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def submit(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def restart(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def flag(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def resolve(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def archive(origTrial: Trial, comment: Option[String])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def unassign(origTrial: Trial)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def addTrial(trial: TrialCreationRequest)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Trial]
}
