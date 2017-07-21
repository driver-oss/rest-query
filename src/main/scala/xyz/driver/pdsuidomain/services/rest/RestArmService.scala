package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.ListResponse
import xyz.driver.pdsuidomain.formats.json.arm.ApiArm
import xyz.driver.pdsuidomain.services.ArmService

class RestArmService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                protected val exec: ExecutionContext)
    extends ArmService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.ArmService._

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/arm", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiArm]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount)
    }
  }

  def getById(armId: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/arm/$armId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiArm](response)
    } yield {
      GetByIdReply.Entity(reply.toDomain)
    }
  }

  def create(draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(ApiArm.fromDomain(draftArm)).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/arm")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiArm](response)
    } yield {
      CreateReply.Created(reply.toDomain)
    }
  }

  def update(origArm: Arm, draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = origArm.id
    val request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiArm](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  def delete(id: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiArm](response)
    } yield {
      DeleteReply.Deleted
    }
  }

}
