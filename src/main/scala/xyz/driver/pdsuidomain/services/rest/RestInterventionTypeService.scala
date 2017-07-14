package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuidomain.formats.json.intervention.ApiInterventionType
import xyz.driver.pdsuidomain.services.InterventionTypeService

class RestInterventionTypeService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: ActorMaterializer,
        protected val exec: ExecutionContext)
    extends InterventionTypeService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.ListResponse
  import xyz.driver.pdsuidomain.services.InterventionTypeService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, "/v1/intervention-type", sortingQuery(sorting)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiInterventionType]](response)
    } yield {
      {
        val domain = reply.items.map(_.toDomain)
        GetListReply.EntityList(domain.toList, reply.meta.itemsCount)
      }
    }
  }

}
