package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.ListResponse
import xyz.driver.pdsuidomain.formats.json.intervention.ApiIntervention
import xyz.driver.pdsuidomain.services.InterventionService

class RestInterventionService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: ActorMaterializer,
        protected val exec: ExecutionContext)
    extends InterventionService with RestHelper {

  import xyz.driver.pdsuidomain.services.InterventionService._
  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._

  // GET   /v1/intervention     xyz.driver.server.controllers.InterventionController.getList
  // GET   /v1/intervention/:id xyz.driver.server.controllers.InterventionController.getById(id: Long)
  // PATCH /v1/intervention/:id xyz.driver.server.controllers.InterventionController.update(id: Long)

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {

    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          "/v1/intervention",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ListResponse[ApiIntervention], GetListReply](response) { api =>
                GetListReply.EntityList(api.items.map(_.toDomain), api.meta.itemsCount)
              }()
    } yield {
      reply
    }
  }

  def getById(id: LongId[Intervention])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/intervention/$id"))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiIntervention, GetByIdReply](response) { api =>
                GetByIdReply.Entity(api.toDomain)
              }()
    } yield {
      reply
    }
  }

  def update(origIntervention: InterventionWithArms, draftIntervention: InterventionWithArms)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origIntervention.intervention.id

    for {
      entity <- Marshal(ApiIntervention.fromDomain(draftIntervention)).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/intervention/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiIntervention, UpdateReply](response) { api =>
                UpdateReply.Updated(api.toDomain)
              }()
    } yield {
      reply
    }
  }

}
