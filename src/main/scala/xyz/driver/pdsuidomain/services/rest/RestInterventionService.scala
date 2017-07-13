package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.marshalling.Marshal
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

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.InterventionService._

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
      reply    <- apiResponse[ListResponse[ApiIntervention]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount)
    }
  }

  def getById(id: LongId[Intervention])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/intervention/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiIntervention](response)
    } yield {
      GetByIdReply.Entity(reply.toDomain)
    }
  }

  def update(origIntervention: InterventionWithArms, draftIntervention: InterventionWithArms)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origIntervention.intervention.id
    for {
      entity <- Marshal(ApiIntervention.fromDomain(draftIntervention)).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/intervention/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiIntervention](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

}
