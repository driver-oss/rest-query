package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.error.DomainError
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

    for {
      response <- transport.sendRequestGetResponse(requestContext)(
                   get(baseUri, "/v1/intervention-type", query = sortingQuery(sorting)))
      reply <- apiResponse[ListResponse[ApiInterventionType], GetListReply](response) { list =>
                val domain = list.items.map(_.toDomain)
                GetListReply.EntityList(domain.toList, list.meta.itemsCount)
              } {
                case _: DomainError.AuthorizationError => GetListReply.AuthorizationError
              }
    } yield {
      reply
    }
  }

}
