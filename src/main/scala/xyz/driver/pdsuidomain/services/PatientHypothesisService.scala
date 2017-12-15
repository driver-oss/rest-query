package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.{Hypothesis, Patient, PatientHypothesis}

import scala.concurrent.Future

trait PatientHypothesisService {

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[PatientHypothesis]]

  def getById(patientId: UuidId[Patient], hypothesisId: UuidId[Hypothesis])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Option[PatientHypothesis]]

  def update(origPatientHypothesis: PatientHypothesis, draftPatientHypothesis: PatientHypothesis)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[PatientHypothesis]
}
