package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.auth.AuthenticatedRequestContext
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{Document, DocumentHistory}

import scala.concurrent.Future

object DocumentHistoryService {

  trait DefaultNotFoundError {
    def userMessage: String = "Document history not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[DocumentHistory], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    final case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

}

trait DocumentHistoryService {

  import DocumentHistoryService._

  def getListByDocumentId(id: LongId[Document],
                          filter: SearchFilterExpr = SearchFilterExpr.Empty,
                          sorting: Option[Sorting] = None,
                          pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply]

}
