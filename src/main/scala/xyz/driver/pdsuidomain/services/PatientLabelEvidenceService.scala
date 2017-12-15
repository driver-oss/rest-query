package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.labels.Label
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

trait PatientLabelEvidenceService {

  def getById(patientId: UuidId[Patient], labelId: LongId[Label], id: LongId[PatientLabelEvidence])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Option[PatientLabelEvidenceView]]

  def getAll(patientId: UuidId[Patient],
             labelId: LongId[Label],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[PatientLabelEvidenceView]]
}
