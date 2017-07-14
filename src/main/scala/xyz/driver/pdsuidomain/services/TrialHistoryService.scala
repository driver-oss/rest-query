package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.StringId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Trial, TrialHistory}

import scala.concurrent.Future

object TrialHistoryService {

  trait DefaultNotFoundError {
    def userMessage: String = "Trial not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[TrialHistory], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    final case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

}

trait TrialHistoryService {

  import TrialHistoryService._

  def getListByTrialId(id: StringId[Trial],
                       filter: SearchFilterExpr = SearchFilterExpr.Empty,
                       sorting: Option[Sorting] = None,
                       pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

}
