package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Hypothesis, Patient, PatientHypothesis}

import scala.concurrent.Future

object PatientHypothesisService {

  trait DefaultNotFoundError {
    def userMessage: String = "Patient hypothesis not found"
  }

  trait DefaultPatientNotFoundError {
    def userMessage: String = "Patient not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[PatientHypothesis], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetListReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case class CommonError(userMessage: String) extends GetListReply with DomainError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: PatientHypothesis) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends GetByIdReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String) extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    case class Updated(updated: PatientHypothesis) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends UpdateReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String) extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x) => phi"Updated($x)"
      case x: Error   => DomainError.toPhiString(x)
    }
  }
}

trait PatientHypothesisService {

  import PatientHypothesisService._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getById(patientId: UuidId[Patient], hypothesisId: UuidId[Hypothesis])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def update(origPatientHypothesis: PatientHypothesis, draftPatientHypothesis: PatientHypothesis)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
