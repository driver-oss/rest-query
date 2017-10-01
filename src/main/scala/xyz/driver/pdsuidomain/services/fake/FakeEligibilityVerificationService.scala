package xyz.driver.pdsuidomain.services.fake

import xyz.driver.core.rest.ServiceRequestContext
import xyz.driver.core.{Id, generators}
import xyz.driver.entities.patient
import xyz.driver.pdsuidomain.entities.eligibility.MismatchRankedLabels
import xyz.driver.pdsuidomain.entities.{Arm, Patient, eligibility}
import xyz.driver.pdsuidomain.services.EligibilityVerificationService

import scala.concurrent.Future
import scalaz.ListT

class FakeEligibilityVerificationService extends EligibilityVerificationService {

  override def getMatchedPatients()(implicit ctx: ServiceRequestContext): ListT[Future, eligibility.MatchedPatient] =
    ListT.listT[Future](
      Future.successful(List(xyz.driver.pdsuidomain.fakes.entities.eligibility.nextMatchedPatient())))

  override def getMismatchRankedLabels(
          patientId: Id[Patient],
          cancerType: patient.CancerType,
          excludedArms: Seq[Id[Arm]])(implicit ctx: ServiceRequestContext): Future[eligibility.MismatchRankedLabels] =
    Future.successful(
      MismatchRankedLabels(
        generators.seqOf(xyz.driver.pdsuidomain.fakes.entities.eligibility.nextLabelMismatchRank()),
        labelVersion = generators.nextInt(10)
      ))
}