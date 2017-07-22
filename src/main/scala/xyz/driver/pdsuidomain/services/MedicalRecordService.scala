package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuicommon.error._
import xyz.driver.pdsuidomain.entities.MedicalRecord.PdfSource
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object MedicalRecordService {

  type PdfSourceFetcher = (String, String) => Future[PdfSource]

  trait DefaultNotFoundError {
    def userMessage: String = "Medical record hasn't been found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetByIdReply
  object GetByIdReply {
    final case class Entity(x: MedicalRecord) extends GetByIdReply

    type Error = GetByIdReply with DomainError

    case object NotFoundError extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError

    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

  sealed trait GetPdfSourceReply
  object GetPdfSourceReply {
    type Error = GetPdfSourceReply with DomainError

    final case class Entity(x: PdfSource.Channel) extends GetPdfSourceReply

    case object AuthorizationError
        extends GetPdfSourceReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object NotFoundError extends GetPdfSourceReply with DomainError.NotFoundError {
      def userMessage: String = "Medical record PDF hasn't been found"
    }

    case object RecordNotFoundError extends GetPdfSourceReply with DomainError.NotFoundError with DefaultNotFoundError

    final case class CommonError(userMessage: String) extends GetPdfSourceReply with DomainError
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[MedicalRecord], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    type Error = GetListReply with DomainError

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object NotFoundError extends GetListReply with DomainError.NotFoundError {
      def userMessage: String = "Patient wasn't found"
    }
  }

  sealed trait CreateReply
  object CreateReply {
    final case class Created(x: MedicalRecord) extends CreateReply
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    final case class Updated(updated: MedicalRecord) extends UpdateReply

    case object NotFoundError extends UpdateReply with DefaultNotFoundError with DomainError.NotFoundError

    case object AuthorizationError
        extends UpdateReply with DefaultAccessDeniedError with DomainError.AuthorizationError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }

  final case class Settings(pdfSourceBucket: String)
}

trait MedicalRecordService {

  import MedicalRecordService._

  def getById(recordId: LongId[MedicalRecord])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def getPatientRecords(patientId: UuidId[Patient])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getPdfSource(recordId: LongId[MedicalRecord])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetPdfSourceReply]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def create(draft: MedicalRecord): Future[CreateReply]

  def update(origRecord: MedicalRecord, draftRecord: MedicalRecord)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def start(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def submit(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def restart(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def flag(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def resolve(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def unassign(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def archive(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
