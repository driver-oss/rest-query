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
import xyz.driver.pdsuidomain.formats.json.message.ApiMessage
import xyz.driver.pdsuidomain.services.MessageService

class RestMessageService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: ActorMaterializer,
        protected val exec: ExecutionContext)
    extends MessageService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.MessageService._

  // GET    /v1/message     xyz.driver.server.controllers.MessageController.getList
  // POST   /v1/message     xyz.driver.server.controllers.MessageController.create
  // PATCH  /v1/message/:id xyz.driver.server.controllers.MessageController.update(id: Long)
  // DELETE /v1/message/:id xyz.driver.server.controllers.MessageController.delete(id: Long)

  def create(draftMessage: Message)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(ApiMessage.fromDomain(draftMessage)).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/message")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiMessage, CreateReply](response) { api =>
                CreateReply.Created(api.toDomain)
              }
    } yield {
      reply
    }
  }

  def getById(messageId: LongId[Message])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    import SearchFilterExpr._
    import SearchFilterBinaryOperation._
    val filter = Atom.Binary("id", Eq, messageId)
    getAll(filter).map {
      case GetListReply.EntityList(messages, _, _) if messages.isEmpty =>
        GetByIdReply.NotFoundError
      case GetListReply.EntityList(messages, _, _) =>
        GetByIdReply.Entity(messages.head)
      case GetListReply.AuthorizationError =>
        GetByIdReply.AuthorizationError
    }
  }
  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {

    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/message", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ListResponse[ApiMessage], GetListReply](response) { api =>
                GetListReply.EntityList(api.items.map(_.toDomain), api.meta.itemsCount, api.meta.lastUpdate)
              }
    } yield {
      reply
    }
  }

  def update(origMessage: Message, draftMessage: Message)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    for {
      entity <- Marshal(ApiMessage.fromDomain(draftMessage)).to[RequestEntity]
      id      = origMessage.id.id
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/message/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiMessage, UpdateReply](response) { api =>
                UpdateReply.Updated(api.toDomain)
              }
    } yield {
      reply
    }
  }

  def delete(messageId: LongId[Message])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/message/${messageId.id}"))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiMessage, DeleteReply](response) { _ =>
                DeleteReply.Deleted
              }
    } yield {
      reply
    }
  }

}
