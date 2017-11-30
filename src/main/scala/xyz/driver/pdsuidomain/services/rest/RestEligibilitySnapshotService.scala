package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.Materializer
import xyz.driver.core.rest.{AuthorizedServiceRequestContext, ServiceTransport}
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.eligibility.EligibleTrial
import xyz.driver.pdsuidomain.entities.{Patient, eligibility}
import xyz.driver.pdsuidomain.services.EligibilitySnapshotService

import scala.concurrent.{ExecutionContext, Future}

class RestEligibilitySnapshotService(transport: ServiceTransport, baseUrl: Uri)(
  implicit protected val materializer: Materializer,
  protected val exec: ExecutionContext) extends EligibilitySnapshotService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._
  import xyz.driver.pdsuidomain.formats.json.eligibility._

  override def eligibilitySnapshot(patientId: UuidId[Patient])
                                  (implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]):
  Future[Seq[eligibility.EligibleTrial]] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUrl, s"/v1/patient/$patientId/eligibilitySnapshot"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Seq[EligibleTrial]](response)
    } yield {
      reply
    }
  }

}
