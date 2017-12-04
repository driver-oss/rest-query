package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.labels.Label
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

trait PatientLabelService {

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[RichPatientLabel], Int)]

  def getDefiningCriteriaList(patientId: UuidId[Patient],
                              hypothesisId: UuidId[Hypothesis],
                              pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[PatientLabel], Int)]

  def getByLabelIdOfPatient(patientId: UuidId[Patient], labelId: LongId[Label])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientLabel]

  def update(origPatientLabel: PatientLabel, draftPatientLabel: PatientLabel)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientLabel]
}
