package xyz.driver.pdsuidomain.services

import xyz.driver.core.Id
import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.patient.{CancerType, Patient}
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuidomain.entities.eligibility.{MatchedPatient, MismatchRankedLabels}
import xyz.driver.pdsuidomain.entities.Arm

import scala.concurrent.Future
import scalaz.ListT

trait EligibilityVerificationService {

  def getMatchedPatients()(implicit ctx: AuthorizedServiceRequestContext[AuthUserInfo]): ListT[Future, MatchedPatient]

  def getMismatchRankedLabels(patientId: Id[Patient], cancerType: CancerType, excludedArms: Seq[Id[Arm]])(
          implicit ctx: AuthorizedServiceRequestContext[AuthUserInfo]): Future[MismatchRankedLabels]
}
