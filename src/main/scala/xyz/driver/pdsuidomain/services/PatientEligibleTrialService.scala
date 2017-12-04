package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.entities._

import scala.concurrent.Future

trait PatientEligibleTrialService {

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[RichPatientEligibleTrial], Int)]

  def getById(patientId: UuidId[Patient], id: LongId[PatientTrialArmGroup])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientEligibleTrial]

  def getCriterionListByGroupId(patientId: UuidId[Patient], id: LongId[PatientTrialArmGroup])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[RichPatientCriterion],
                                                                                           Int)]

  def update(origEligibleTrialWithTrial: RichPatientEligibleTrial, draftPatientTrialArmGroup: PatientTrialArmGroupView)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientEligibleTrial]

}
