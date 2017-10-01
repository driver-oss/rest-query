package xyz.driver.pdsuidomain.services

import xyz.driver.core.Id
import xyz.driver.core.rest.ServiceRequestContext
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuidomain.entities.eligibility.{MatchedPatient, MismatchRankedLabels}
import xyz.driver.pdsuidomain.entities.{Arm, Patient}

import scala.concurrent.Future
import scalaz.ListT

trait EligibilityVerificationService {

  def getMatchedPatients()(implicit ctx: ServiceRequestContext): ListT[Future, MatchedPatient]

  def getMismatchRankedLabels(patientId: Id[Patient], cancerType: CancerType, excludedArms: Seq[Id[Arm]])(
          implicit ctx: ServiceRequestContext): Future[MismatchRankedLabels]
}
