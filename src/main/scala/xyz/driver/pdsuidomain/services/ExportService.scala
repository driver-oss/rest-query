package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AnonymousRequestContext
import xyz.driver.pdsuicommon.db.SearchFilterExpr
import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Patient, Trial}
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientWithLabels
import xyz.driver.pdsuidomain.entities.export.trial.{ExportTrial, ExportTrialWithLabels}

import scala.concurrent.Future

object ExportService {

  sealed trait GetPatientReply
  object GetPatientReply {
    type Error = GetPatientReply with DomainError

    case class Entity(x: ExportPatientWithLabels) extends GetPatientReply

    case object NotFoundError extends GetPatientReply with DomainError.NotFoundError {
      def userMessage: String = "Patient not found"
    }
  }

  sealed trait GetTrialListReply
  object GetTrialListReply {
    case class EntityList(xs: Seq[ExportTrial], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetTrialListReply
  }

  sealed trait GetTrialReply
  object GetTrialReply {
    type Error = GetTrialReply with DomainError

    case class Entity(x: ExportTrialWithLabels) extends GetTrialReply

    case object NotFoundError extends GetTrialReply with DomainError.NotFoundError {
      def userMessage: String = "Trial not found"
    }
  }
}

trait ExportService extends PhiLogging {

  import ExportService._

  def getPatient(id: UuidId[Patient])(implicit requestContext: AnonymousRequestContext): Future[GetPatientReply]

  def getTrialList(filter: SearchFilterExpr = SearchFilterExpr.Empty)(
          implicit requestContext: AnonymousRequestContext): Future[GetTrialListReply]

  def getTrial(trialId: StringId[Trial], condition: String)(
          implicit requestContext: AnonymousRequestContext): Future[GetTrialReply]

}
