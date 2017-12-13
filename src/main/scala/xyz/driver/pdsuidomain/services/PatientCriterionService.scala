package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

trait PatientCriterionService {

  def getAll(patientId: UuidId[Patient],
             origFilter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[RichPatientCriterion]]

  def getById(patientId: UuidId[Patient], id: LongId[PatientCriterion])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientCriterion]

  def updateList(patientId: UuidId[Patient], draftEntities: List[DraftPatientCriterion])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Unit]

  def update(origEntity: PatientCriterion, draftEntity: PatientCriterion, patientId: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientCriterion]
}
