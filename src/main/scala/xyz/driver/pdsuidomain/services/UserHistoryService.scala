package xyz.driver.pdsuidomain.services

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.UserHistory

import scala.concurrent.Future

object UserHistoryService {

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[UserHistory], totalFound: Int) extends GetListReply

    case object AuthorizationError extends GetListReply with DomainError.AuthorizationError {
      def userMessage: String = "Access denied"
    }
  }
}

trait UserHistoryService {

  import UserHistoryService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]
}
