package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.entities.export.patient.ExportPatientWithLabels
import xyz.driver.pdsuidomain.services.ExtractedDataService
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.formats.json.export._

class RestExtractedDataService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends ExtractedDataService with RestHelper {

  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.extracteddata._
  import xyz.driver.pdsuidomain.services.ExtractedDataService._

  def getById(id: LongId[ExtractedData])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/extracted-data/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichExtractedData](response)
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
                                          "/v1/extracted-data",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichExtractedData]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def create(draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[CreateReply] = {
    for {
      entity <- Marshal(draftRichExtractedData).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/extracted-data")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichExtractedData](response)
    } yield {
      CreateReply.Created(reply)
    }
  }
  def update(origRichExtractedData: RichExtractedData, draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    val id = origRichExtractedData.extractedData.id
    for {
      entity <- Marshal(draftRichExtractedData).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/extracted-data/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichExtractedData](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def delete(id: LongId[ExtractedData])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/export-data/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

  def getPatientLabels(id: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetPatientLabelsReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/export/patient/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ExportPatientWithLabels](response)
    } yield {
      GetPatientLabelsReply.Entity(reply)
    }
  }

}
