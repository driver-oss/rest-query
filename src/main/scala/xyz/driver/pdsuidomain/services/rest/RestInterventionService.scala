package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.InterventionService

class RestInterventionService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends InterventionService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.intervention._
  import xyz.driver.pdsuidomain.services.InterventionService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          "/v1/intervention",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[InterventionWithArms]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def getById(id: LongId[Intervention])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/intervention/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[InterventionWithArms](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def update(origIntervention: InterventionWithArms, draftIntervention: InterventionWithArms)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    val id = origIntervention.intervention.id
    for {
      entity <- Marshal(draftIntervention).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/intervention/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[InterventionWithArms](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def create(draftIntervention: InterventionWithArms)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply] = {
    for {
      entity <- Marshal(draftIntervention).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/intervention")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[InterventionWithArms](response)
    } yield {
      CreateReply.Created(reply)
    }
  }

  def delete(id: LongId[Intervention])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/intervention/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

}
