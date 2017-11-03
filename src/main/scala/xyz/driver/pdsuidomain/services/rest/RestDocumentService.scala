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
import xyz.driver.pdsuidomain.services.DocumentService

class RestDocumentService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                     protected val exec: ExecutionContext)
    extends DocumentService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.document._
  import xyz.driver.pdsuidomain.services.DocumentService._

  def getById(id: LongId[Document])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/document/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Document](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply] = {

    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          "/v1/document",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[Document]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  def create(draftDocument: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply] = {
    for {
      entity <- Marshal(draftDocument).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/document")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Document](response)
    } yield {
      CreateReply.Created(reply)
    }
  }

  def update(orig: Document, draft: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    for {
      entity <- Marshal(draft).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/document/${orig.id}")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Document](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def delete(id: LongId[Document])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/document/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

  private def editAction(orig: Document, action: String)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    val id      = orig.id.toString
    val request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, s"/v1/document/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[Document](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def start(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "start")
  def submit(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "submit")
  def restart(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "restart")
  def flag(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "flag")
  def resolve(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "resolve")
  def unassign(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "unassign")
  def archive(orig: Document)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] =
    editAction(orig, "archive")

}
