package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.{Patient, PatientHistory}

import scala.concurrent.Future

trait PatientHistoryService {

  def getListByPatientId(id: UuidId[Patient],
                         filter: SearchFilterExpr = SearchFilterExpr.Empty,
                         sorting: Option[Sorting] = None,
                         pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[PatientHistory]]

}
