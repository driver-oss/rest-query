package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import scala.NotImplementedError
import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.trial.ApiTrial
import xyz.driver.pdsuidomain.services.TrialService
import xyz.driver.pdsuidomain.formats.json.ListResponse

class RestTrialService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: ActorMaterializer,
        protected val exec: ExecutionContext)
    extends TrialService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.TrialService._

  // GET               /v1/trial                  xyz.driver.server.controllers.TrialController.getList
  // GET               /v1/trial/:id              xyz.driver.server.controllers.TrialController.getById(id: String)
  // GET               /v1/trial/:id/source       xyz.driver.server.controllers.TrialController.getSource(id: String)
  // PATCH             /v1/trial/:id              xyz.driver.server.controllers.TrialController.update(id: String)
  // POST              /v1/trial/:id/start        xyz.driver.server.controllers.TrialController.start(id: String)
  // POST              /v1/trial/:id/submit       xyz.driver.server.controllers.TrialController.submit(id: String)
  // POST              /v1/trial/:id/restart      xyz.driver.server.controllers.TrialController.restart(id: String)
  // POST              /v1/trial/:id/flag         xyz.driver.server.controllers.TrialController.flag(id: String)
  // POST              /v1/trial/:id/resolve      xyz.driver.server.controllers.TrialController.resolve(id: String)
  // POST              /v1/trial/:id/archive      xyz.driver.server.controllers.TrialController.archive(id: String)
  // POST              /v1/trial/:id/unassign     xyz.driver.server.controllers.TrialController.unassign(id: String)

  def getById(id: StringId[Trial])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiTrial, GetByIdReply](response) { api =>
                GetByIdReply.Entity(api.toDomain)
              }()
    } yield {
      reply
    }
  }

  def getPdfSource(trialId: StringId[Trial])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetPdfSourceReply] = Future.failed(
    new NotImplementedError("Streaming PDF over network is not supported.")
  )

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {

    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/trial", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ListResponse[ApiTrial], GetListReply](response) { api =>
                GetListReply.EntityList(api.items.map(_.toDomain), api.meta.itemsCount, api.meta.lastUpdate)
              }()
    } yield {
      reply
    }
  }

  def update(origTrial: Trial, draftTrial: Trial)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id = origTrial.id.id
    for {
      entity <- Marshal(ApiTrial.fromDomain(draftTrial)).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/trial/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiTrial, UpdateReply](response) { api =>
                UpdateReply.Updated(api.toDomain)
              }()
    } yield {
      reply
    }
  }

  private def singleAction(origTrial: Trial, action: String)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = origTrial.id.id
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ApiTrial, UpdateReply](response) { api =>
                UpdateReply.Updated(api.toDomain)
              }()
    } yield {
      reply
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
