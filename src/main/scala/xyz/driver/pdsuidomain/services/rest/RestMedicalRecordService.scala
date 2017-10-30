package xyz.driver.pdsuidomain.services.rest

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.MedicalRecordService

class RestMedicalRecordService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends MedicalRecordService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.record._
  import xyz.driver.pdsuidomain.services.MedicalRecordService._

  def getById(recordId: LongId[MedicalRecord])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/record/$recordId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[MedicalRecord](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def getPdfSource(recordId: LongId[MedicalRecord])(
          implicit requestContext: AuthenticatedRequestContext): Future[Source[ByteString, NotUsed]] = {

    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/record/${recordId}/source"))

    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[HttpEntity](response)
    } yield {
      reply.dataBytes.mapMaterializedValue(_ => NotUsed)
    }
  }

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {

    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/record", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[MedicalRecord]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  def create(draftRecord: MedicalRecord)(implicit requestContext: AnonymousRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(draftRecord).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/record")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[MedicalRecord](response)
    } yield {
      CreateReply.Created(reply)
    }
  }

  def update(origRecord: MedicalRecord, draftRecord: MedicalRecord)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origRecord.id.toString
    for {
      entity <- Marshal(draftRecord).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/record/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[MedicalRecord](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  private def editAction(orig: MedicalRecord, action: String)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = orig.id.toString
    val request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, s"/v1/record/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[MedicalRecord](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def start(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "start")
  def submit(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "submit")
  def restart(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "restart")
  def flag(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "flag")
  def resolve(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "resolve")
  def unassign(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "unassign")
  def archive(orig: MedicalRecord)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(orig, "archive")

}
