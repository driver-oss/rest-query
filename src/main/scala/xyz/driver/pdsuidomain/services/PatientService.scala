package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

trait PatientService {

  def getById(id: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Option[Patient]]

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[Patient]]

  def unassign(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]

  def start(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]

  def submit(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]

  def restart(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]

  def flag(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]

  def resolve(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient]
}
