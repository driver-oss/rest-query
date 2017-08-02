package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Patient, PatientHistory}

import scala.concurrent.Future

object PatientHistoryService {

  trait DefaultNotFoundError {
    def userMessage: String = "Patient history not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[PatientHistory],
                                totalFound: Int,
                                lastUpdate: Option[LocalDateTime])
        extends GetListReply

    final case object AuthorizationError
        extends GetListReply
        with DomainError.AuthorizationError
        with DefaultAccessDeniedError
  }

}

trait PatientHistoryService {

  import PatientHistoryService._

  def getListByPatientId(id: UuidId[Patient],
                         filter: SearchFilterExpr = SearchFilterExpr.Empty,
                         sorting: Option[Sorting] = None,
                         pagination: Option[Pagination] = None)(
      implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

}
