package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.InterventionType
import xyz.driver.pdsuidomain.services.InterventionTypeService

class RestInterventionTypeService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends InterventionTypeService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.intervention.interventionTypeFormat
  import xyz.driver.pdsuidomain.services.InterventionTypeService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, "/v1/intervention-type", sortingQuery(sorting)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[InterventionType]](response)
    } yield {
      {
        val domain = reply.items
        GetListReply.EntityList(domain.toList, reply.meta.itemsCount)
      }
    }
  }

}
