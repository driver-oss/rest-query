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
import xyz.driver.pdsuidomain.formats.json.export.ApiExportTrialWithLabels
import xyz.driver.pdsuidomain.formats.json.trial.ApiTrial
import xyz.driver.pdsuidomain.services.TrialService

class RestTrialService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                  protected val exec: ExecutionContext)
    extends TrialService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.TrialService._

  def getById(id: StringId[Trial])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiTrial](response)
    } yield {
      GetByIdReply.Entity(reply.toDomain)
    }
  }

  def getTrialWithLabels(trialId: StringId[Trial], condition: String)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetTrialWithLabelsReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/export/trial/$trialId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiExportTrialWithLabels](response)
    } yield {
      GetTrialWithLabelsReply.Entity(reply.toDomain)
    }
  }

  def getPdfSource(trialId: StringId[Trial])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetPdfSourceReply] =
    Future.failed(new NotImplementedError("Streaming PDF over network is not supported."))

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/trial", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiTrial]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  def update(origTrial: Trial, draftTrial: Trial)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origTrial.id.id
    for {
      entity <- Marshal(ApiTrial.fromDomain(draftTrial)).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/trial/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiTrial](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  private def singleAction(origTrial: Trial, action: String)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = origTrial.id.id
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiTrial](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  def start(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "start")
  def submit(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "submit")
  def restart(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "restart")
  def flag(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "flag")
  def resolve(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "resolve")
  def archive(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "archive")
  def unassign(origTrial: Trial)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    singleAction(origTrial, "unassign")
}
