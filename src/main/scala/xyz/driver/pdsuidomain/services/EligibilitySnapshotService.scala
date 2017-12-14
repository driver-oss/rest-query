package xyz.driver.pdsuidomain.services

import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.{Patient => PdsuiPatient}
import xyz.driver.pdsuidomain.entities.eligibility.EligibleTrial

import scala.concurrent.Future

trait EligibilitySnapshotService {

  def eligibilitySnapshot(patientId: UuidId[PdsuiPatient])(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Seq[EligibleTrial]]

}
