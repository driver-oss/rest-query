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
import xyz.driver.pdsuidomain.formats.json.export.ApiExportPatientWithLabels
import xyz.driver.pdsuidomain.formats.json.extracteddata.ApiExtractedData
import xyz.driver.pdsuidomain.services.ExtractedDataService

class RestExtractedDataService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends ExtractedDataService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.ExtractedDataService._

  def getById(id: LongId[ExtractedData])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/extracted-data/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiExtractedData](response)
    } yield {
      GetByIdReply.Entity(reply.toDomain)
    }
  }

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          "/v1/extracted-data",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiExtractedData]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount)
    }
  }

  def create(draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthenticatedRequestContext): Future[CreateReply] = {
    for {
      entity <- Marshal(ApiExtractedData.fromDomain(draftRichExtractedData)).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, "/v1/extracted-data")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiExtractedData](response)
    } yield {
      CreateReply.Created(reply.toDomain)
    }
  }
  def update(origRichExtractedData: RichExtractedData, draftRichExtractedData: RichExtractedData)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origRichExtractedData.extractedData.id
    for {
      entity <- Marshal(ApiExtractedData.fromDomain(draftRichExtractedData)).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/extracted-data/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiExtractedData](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  def delete(id: LongId[ExtractedData])(implicit requestContext: AuthenticatedRequestContext): Future[DeleteReply] = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/export-data/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

  def getPatientLabels(id: UuidId[Patient])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetPatientLabelsReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/export/patient/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiExportPatientWithLabels](response)
    } yield {
      GetPatientLabelsReply.Entity(reply.toDomain)
    }
  }

}
