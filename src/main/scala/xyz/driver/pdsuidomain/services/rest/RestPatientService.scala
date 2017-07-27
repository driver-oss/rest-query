package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.formats.json.ListResponse
import xyz.driver.pdsuidomain.formats.json.patient.ApiPatient
import xyz.driver.pdsuidomain.services.PatientService

class RestPatientService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                    protected val exec: ExecutionContext)
    extends PatientService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.PatientService._

  def getById(id: UuidId[Patient])(implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiPatient](response)
    } yield {
      GetByIdReply.Entity(reply.toDomain)
    }
  }

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/patient", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[ApiPatient]](response)
    } yield {
      GetListReply.EntityList(reply.items.map(_.toDomain), reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  private def editAction(orig: Patient, action: String)(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    val id      = orig.id.toString
    val request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, s"/v1/patient/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ApiPatient](response)
    } yield {
      UpdateReply.Updated(reply.toDomain)
    }
  }

  def unassign(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "unassign")
  def start(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "start")
  def submit(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "submit")
  def restart(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "restart")
  def flag(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "flag")
  def resolve(origPatient: Patient)(implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] =
    editAction(origPatient, "resolve")
}
