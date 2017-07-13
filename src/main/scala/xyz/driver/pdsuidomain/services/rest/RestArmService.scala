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
import xyz.driver.pdsuidomain.formats.json.arm.ApiArm
import xyz.driver.pdsuidomain.services.ArmService

class RestArmService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: ActorMaterializer,
                                                                protected val exec: ExecutionContext)
    extends ArmService with RestHelper {

  import xyz.driver.pdsuidomain.services.ArmService._
  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._

  // GET               /v1/arm                    xyz.driver.server.controllers.ArmController.getList
  // GET               /v1/arm/:id                xyz.driver.server.controllers.ArmController.getById(id: Long)
  // POST              /v1/arm                    xyz.driver.server.controllers.ArmController.create
  // PATCH             /v1/arm/:id                xyz.driver.server.controllers.ArmController.update(id: Long)
  // DELETE            /v1/arm/:id                xyz.driver.server.controllers.ArmController.delete(id: Long)

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {

    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/arm", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ListResponse[ApiArm], GetListReply](response) { api =>
                GetListReply.EntityList(api.items.map(_.toDomain), api.meta.itemsCount)
              }
    } yield {
      reply
    }
  }

  def getById(armId: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/arm/$armId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiArm, GetByIdReply](response) { api =>
                GetByIdReply.Entity(api.toDomain)
              }
    } yield {
      reply
    }
  }

  def create(draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(ApiArm.fromDomain(draftArm)).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/arm")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiArm, CreateReply](response) { api =>
                CreateReply.Created(api.toDomain)
              }
    } yield {
      reply
    }
  }

  def update(origArm: Arm, draftArm: Arm)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = origArm.id
    val request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiArm, UpdateReply](response) { api =>
                UpdateReply.Updated(api.toDomain)
              }
    } yield {
      reply
    }
  }

  def delete(id: LongId[Arm])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/arm/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiArm, DeleteReply](response) { _ =>
                DeleteReply.Deleted
              }
    } yield {
      reply
    }
  }

}
