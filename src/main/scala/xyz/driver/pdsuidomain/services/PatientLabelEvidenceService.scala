package xyz.driver.pdsuidomain.services

import java.time.LocalDate

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object PatientLabelEvidenceService {

  case class Aggregated(evidence: PatientLabelEvidence,
                        date: LocalDate,
                        documentType: String,
                        providerType: String)

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    case class Entity(x: Aggregated) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case class NotFoundError(userMessage: String) extends GetByIdReply
      with DomainError.NotFoundError

    case class CommonError(userMessage: String) extends GetByIdReply
      with DomainError

    case object AuthorizationError extends GetByIdReply
      with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetListReply
  object GetListReply {
    case class EntityList(xs: Seq[Aggregated], totalFound: Int)
      extends GetListReply

    type Error = GetListReply with DomainError

    case object AuthorizationError
      extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case class CommonError(userMessage: String) extends GetListReply
      with DomainError
  }
}

trait PatientLabelEvidenceService {

  import PatientLabelEvidenceService._

  def getById(patientId: UuidId[Patient],
              labelId: LongId[Label],
              id: LongId[PatientLabelEvidence])
             (implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getAll(patientId: UuidId[Patient],
             labelId: LongId[Label],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)
            (implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]
}
