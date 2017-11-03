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
import xyz.driver.pdsuidomain.services.ArmService

class RestArmService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                protected val exec: ExecutionContext)
    extends ArmService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.arm._
  import xyz.driver.pdsuidomain.services.ArmService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply] = {
    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/arm", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[Arm]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def getById(armId: LongId[Arm])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/arm/$armId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Arm](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def create(draftArm: Arm)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply] = {
    for {
      entity <- Marshal(draftArm).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/arm")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Arm](response)
    } yield {
      CreateReply.Created(reply)
    }
  }

  def update(origArm: Arm, draftArm: Arm)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    val id      = origArm.id
    val request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Arm](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def delete(id: LongId[Arm])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

}
