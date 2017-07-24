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
import xyz.driver.pdsuidomain.formats.json.message.ApiMessage
import xyz.driver.pdsuidomain.services.MessageService

class RestMessageService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                    protected val exec: ExecutionContext)
    extends MessageService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.MessageService._

  def create(draftMessage: Message)(implicit requestContext: AuthenticatedRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(ApiMessage.fromDomain(draftMessage)).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/message")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiMessage](response)
    } yield {
      CreateReply.Created(reply.toDomain)
    }
  }

  def getById(messageId: LongId[Message])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    import xyz.driver.pdsuicommon.db.SearchFilterBinaryOperation._
    import xyz.driver.pdsuicommon.db.SearchFilterExpr._
    val filter = Atom.Binary("id", Eq, messageId)
    getAll(filter).map({
      case GetListReply.EntityList(messages, _, _) if messages.isEmpty =>
        GetByIdReply.NotFoundError
      case GetListReply.EntityList(messages, _, _) =>
        GetByIdReply.Entity(messages.head)
      case GetListReply.AuthorizationError =>
        GetByIdReply.AuthorizationError
    })
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
      reply    <- apiResponse[ListResponse[ApiMessage]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  def update(origMessage: Message, draftMessage: Message)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    for {
      entity <- Marshal(ApiMessage.fromDomain(draftMessage)).to[RequestEntity]
      id      = origMessage.id.id
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/message/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiMessage](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  def delete(messageId: LongId[Message])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/message/${messageId.id}"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

}
