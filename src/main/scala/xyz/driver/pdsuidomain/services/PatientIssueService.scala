package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.{Patient, PatientIssue}

import scala.concurrent.Future

trait PatientIssueService {

  def create(draft: PatientIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[PatientIssue]

  def getById(patientId: UuidId[Patient], id: LongId[PatientIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Option[PatientIssue]]

  def getListByPatientId(patientId: UuidId[Patient],
                         filter: SearchFilterExpr = SearchFilterExpr.Empty,
                         sorting: Option[Sorting] = None,
                         pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[PatientIssue]]

  def update(orig: PatientIssue, draft: PatientIssue)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[PatientIssue]

  def delete(patientId: UuidId[Patient], id: LongId[PatientIssue])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Unit]

}
