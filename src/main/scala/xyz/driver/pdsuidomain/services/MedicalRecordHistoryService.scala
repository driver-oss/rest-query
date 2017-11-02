package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.error.DomainError
import xyz.driver.pdsuidomain.entities.{MedicalRecord, MedicalRecordHistory}

import scala.concurrent.Future

object MedicalRecordHistoryService {

  trait DefaultNotFoundError {
    def userMessage: String = "Medical record history not found"
  }

  trait DefaultAccessDeniedError {
    def userMessage: String = "Access denied"
  }

  sealed trait GetListReply
  object GetListReply {
    final case class EntityList(xs: Seq[MedicalRecordHistory], totalFound: Int, lastUpdate: Option[LocalDateTime])
        extends GetListReply

    final case object AuthorizationError
        extends GetListReply with DomainError.AuthorizationError with DefaultAccessDeniedError
  }

}

trait MedicalRecordHistoryService {

  import MedicalRecordHistoryService._

  def getListByRecordId(id: LongId[MedicalRecord],
                        filter: SearchFilterExpr = SearchFilterExpr.Empty,
                        sorting: Option[Sorting] = None,
                        pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply]

}
