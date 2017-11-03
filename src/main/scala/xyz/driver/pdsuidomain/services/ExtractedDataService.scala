package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientWithLabels

import scala.concurrent.Future

object ExtractedDataService {

  trait DefaultNotFoundError {
    def userMessage: String = "Extracted data hasn't been found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  final case class RichExtractedData(extractedData: ExtractedData, labels: List[ExtractedDataLabel])

  object RichExtractedData {
    implicit def toPhiString(x: RichExtractedData): PhiString = {
      import x._
      phi"RichExtractedData(extractedData=$extractedData, labels=$labels)"
    }
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError
    final case class Entity(x: RichExtractedData) extends GetByIdReply

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object NotFoundError extends GetByIdReply with DefaultNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[RichExtractedData], totalFound: Int) extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetPatientLabelsReply
  object GetPatientLabelsReply {
    type Error = GetPatientLabelsReply with DomainError

    final case class Entity(x: ExportPatientWithLabels) extends GetPatientLabelsReply

    case object NotFoundError extends GetPatientLabelsReply with DomainError.NotFoundError {
      def userMessage: String = "Patient not found"
    }
  }

  sealed trait CreateReply
  object CreateReply {
    type Error = CreateReply with DomainError
    final case class Created(x: RichExtractedData) extends CreateReply

    case object AuthorizationError
        extends CreateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    final case class CommonError(userMessage: String) extends CreateReply with DomainError
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError
    final case class Updated(updated: RichExtractedData) extends UpdateReply

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }

  sealed trait DeleteReply
  object DeleteReply {
    type Error = DeleteReply with DomainError
    case object Deleted extends DeleteReply

    case object AuthorizationError
        extends DeleteReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    case object NotFoundError extends DeleteReply with DefaultNotFoundError with DomainError.NotFoundError

    final case class CommonError(userMessage: String) extends DeleteReply with DomainError
  }
}

trait ExtractedDataService {

  import ExtractedDataService._

  def getById(id: LongId[ExtractedData])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply]

  def getPatientLabels(id: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetPatientLabelsReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply]

  def create(draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply]

  def update(origRichExtractedData: RichExtractedData, draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply]

  def delete(id: LongId[ExtractedData])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply]
}
