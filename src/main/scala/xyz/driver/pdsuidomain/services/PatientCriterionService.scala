package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.entities.labels.{Label, LabelValue}
import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

object PatientCriterionService {

  final case class DraftPatientCriterion(id: LongId[PatientCriterion],
                                         eligibilityStatus: Option[LabelValue],
                                         isVerified: Option[Boolean]) {
    def applyTo(orig: PatientCriterion) = {
      orig.copy(
        eligibilityStatus = eligibilityStatus.orElse(orig.eligibilityStatus),
        isVerified = isVerified.getOrElse(orig.isVerified)
      )
    }
  }

  trait DefaultPatientNotFoundError {
    def userMessage: String = "Patient not found"
  }

  trait DefaultNotFoundError {
    def userMessage: String = "Patient criterion not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  final case class RichPatientCriterion(patientCriterion: PatientCriterion,
                                        labelId: LongId[Label],
                                        armList: List[PatientCriterionArm])

  object RichPatientCriterion {
    implicit def toPhiString(x: RichPatientCriterion): PhiString = {
      phi"RichPatientCriterion(patientCriterion=${x.patientCriterion}, labelId=${x.labelId}, arms=${x.armList})"
    }
  }

  sealed trait GetListReply
  object GetListReply {
    type Error = GetListReply with DomainError

    final case class EntityList(xs: Seq[RichPatientCriterion], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends GetListReply with DomainError.NotFoundError with DefaultPatientNotFoundError

    final case class CommonError(userMessage: String) extends GetListReply with DomainError

  }

  sealed trait GetByIdReply
  object GetByIdReply {
    type Error = GetByIdReply with DomainError

    final case class Entity(x: RichPatientCriterion) extends GetByIdReply

    case object AuthorizationError
        extends GetByIdReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object NotFoundError extends GetByIdReply with DomainError.NotFoundError with DefaultNotFoundError

    case object PatientNotFoundError
        extends GetByIdReply with DomainError.NotFoundError with DefaultPatientNotFoundError

    final case class CommonError(userMessage: String) extends GetByIdReply with DomainError

    implicit def toPhiString(reply: GetByIdReply): PhiString = reply match {
      case x: DomainError => phi"GetByIdReply.Error($x)"
      case Entity(x)      => phi"GetByIdReply.Entity($x)"
    }
  }

  sealed trait UpdateReply
  object UpdateReply {
    type Error = UpdateReply with DomainError

    final case class Updated(x: RichPatientCriterion) extends UpdateReply

    case object UpdatedList extends UpdateReply

    case object AuthorizationError
        extends UpdateReply with DomainError.AuthorizationError with DefaultAccessDeniedError

    case object PatientNotFoundError
        extends UpdateReply with DomainError.NotFoundError with DefaultPatientNotFoundError

    final case class CommonError(userMessage: String) extends UpdateReply with DomainError
  }
}

trait PatientCriterionService {

  import PatientCriterionService._

  def getAll(patientId: UuidId[Patient],
             origFilter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

  def getById(patientId: UuidId[Patient], id: LongId[PatientCriterion])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply]

  def updateList(patientId: UuidId[Patient], draftEntities: List[DraftPatientCriterion])(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]

  def update(origEntity: PatientCriterion, draftEntity: PatientCriterion, patientId: UuidId[Patient])(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply]
}
