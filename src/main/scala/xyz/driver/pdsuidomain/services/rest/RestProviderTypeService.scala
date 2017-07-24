package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuidomain.formats.json.ListResponse
import xyz.driver.pdsuidomain.services.ProviderTypeService
import xyz.driver.pdsuidomain.formats.json.document.ApiProviderType

class RestProviderTypeService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends ProviderTypeService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.ProviderTypeService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, "/v1/provider-type", sortingQuery(sorting)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiProviderType]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount)
    }
  }

}
