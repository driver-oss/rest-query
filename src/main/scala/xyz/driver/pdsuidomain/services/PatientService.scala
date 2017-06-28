package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object PatientService {

  trait DefaultNotFoundError {
    def userMessage: String = "Patient not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[Patient], totalFound: Int, lastUpdate: Option[LocalDateTime])
      extends GetListReply

    case object AuthorizationError
      extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: Patient) extends GetByIdReply

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

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    case class Updated(updated: Patient) extends UpdateReply

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

trait PatientService {

  import PatientService._

  def getById(id: UuidId[Patient])
             (implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)
            (implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def unassign(origPatient: Patient)
              (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def start(origPatient: Patient)
           (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def submit(origPatient: Patient)
            (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def restart(origPatient: Patient)
             (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def flag(origPatient: Patient)
          (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def resolve(origPatient: Patient)
             (implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
