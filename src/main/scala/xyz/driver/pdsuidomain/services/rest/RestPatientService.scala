package xyz.driver.pdsuidomain.services.rest

import java.time.LocalDateTime

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientService

class RestPatientService(transport: ServiceTransport, baseUri: Uri)(implicit protected val materializer: Materializer,
                                                                    protected val exec: ExecutionContext)
    extends PatientService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.patient._

  def getById(id: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[Patient](response)
    } yield {
      entity
    }
  }

  def getAll(filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[(Seq[Patient], Int, Option[LocalDateTime])] = {
    val request = HttpRequest(
      HttpMethods.GET,
      endpointUri(baseUri, "/v1/patient", filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[Patient]](response)
    } yield {
      (reply.items, reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  private def editAction(orig: Patient, action: String)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] = {
    val id      = orig.id.toString
    val request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, s"/v1/patient/$id/$action"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[Patient](response)
    } yield {
      entity
    }
  }

  def unassign(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "unassign")
  def start(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "start")
  def submit(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "submit")
  def restart(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "restart")
  def flag(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "flag")
  def resolve(origPatient: Patient)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Patient] =
    editAction(origPatient, "resolve")
}
