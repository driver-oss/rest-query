package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object PatientLabelService {

  trait DefaultNotFoundError {
    def userMessage: String = "Patient label not found"
  }

  trait DefaultPatientNotFoundError {
    def userMessage: String = "Patient not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[(PatientLabel, Boolean)], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetListReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case class CommonError(userMessage: String) extends GetListReply with DomainError
  }

  sealed trait GetDefiningCriteriaListReply
  object GetDefiningCriteriaListReply {
    case class EntityList(xs: Seq[PatientLabel], totalFound: Int) extends GetDefiningCriteriaListReply

    case object AuthorizationError
        extends GetDefiningCriteriaListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetDefiningCriteriaListReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case class CommonError(userMessage: String) extends GetDefiningCriteriaListReply with DomainError
  }

  sealed trait GetByLabelIdReply
  object GetByLabelIdReply {
    case class Entity(x: PatientLabel, isVerified: Boolean) extends GetByLabelIdReply

    type Error = GetByLabelIdReply with DomainError

    case object NotFoundError extends GetByLabelIdReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends GetByLabelIdReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends GetByLabelIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String) extends GetByLabelIdReply with DomainError

    implicit def toPhiString(reply: GetByLabelIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x, y)   => phi"GetByIdReply.Entity($x, $y)"
    }
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    case class Updated(updated: PatientLabel, isVerified: Boolean) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object PatientNotFoundError
        extends UpdateReply with DefaultPatientNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String) extends UpdateReply with DomainError

    implicit def toPhiString(reply: UpdateReply): PhiString = reply match {
      case Updated(x, y) => phi"Updated($x, $y)"
      case x: Error      => DomainError.toPhiString(x)
    }
  }
}

trait PatientLabelService {

  import PatientLabelService._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getDefiningCriteriaList(patientId: UuidId[Patient],
                              hypothesisId: UuidId[Hypothesis],
                              pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetDefiningCriteriaListReply]

  def getByLabelIdOfPatient(patientId: UuidId[Patient], labelId: LongId[Label])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByLabelIdReply]

  def update(origPatientLabel: PatientLabel, draftPatientLabel: PatientLabel)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
