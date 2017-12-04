package xyz.driver.pdsuidomain.services

import java.time.LocalDateTime

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.{Patient, PatientHistory}

import scala.concurrent.Future

trait PatientHistoryService {

  def getListByPatientId(id: UuidId[Patient],
                         filter: SearchFilterExpr = SearchFilterExpr.Empty,
                         sorting: Option[Sorting] = None,
                         pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[PatientHistory], Int, Option[LocalDateTime])]

}
