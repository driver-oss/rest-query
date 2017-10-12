package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import spray.json.DefaultJsonProtocol
import xyz.driver.core.Id
import xyz.driver.core.rest.{AuthorizedServiceRequestContext, RestService, ServiceTransport}
import xyz.driver.entities.patient
import xyz.driver.entities.patient.Patient
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.eligibility.{MatchedPatient, MismatchRankedLabels}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, eligibility}
import xyz.driver.pdsuidomain.services.EligibilityVerificationService

import scala.concurrent.{ExecutionContext, Future}
import scalaz.ListT
import scalaz.Scalaz.futureInstance

class RestEligibilityVerificationService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext
) extends EligibilityVerificationService with RestService {

  import DefaultJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.sprayformats.eligibility._

  override def getMatchedPatients()(
          implicit ctx: AuthorizedServiceRequestContext[AuthUserInfo]): ListT[Future, eligibility.MatchedPatient] = {
    val request = get(baseUri, s"/v1/patients")
    listResponse[MatchedPatient](transport.sendRequest(ctx)(request))
  }

  override def getMismatchRankedLabels(patientId: Id[Patient],
                                       cancerType: patient.CancerType,
                                       excludedArms: Seq[LongId[EligibilityArm]])(
          implicit ctx: AuthorizedServiceRequestContext[AuthUserInfo]): Future[eligibility.MismatchRankedLabels] = {

    val query =
      Seq("disease" -> cancerType.toString.toUpperCase, "ineligible_arms" -> excludedArms.map(_.id).mkString(","))

    val request = get(baseUri, s"/v1/patients/$patientId/labels", query)
    optionalResponse[MismatchRankedLabels](transport.sendRequest(ctx)(request))
      .getOrElse(throw new Exception(s"The data of patient $patientId is not ready yet"))
  }
}
