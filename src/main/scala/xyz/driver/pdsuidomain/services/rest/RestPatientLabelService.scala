package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.entities.labels.Label
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, _}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientLabelService

import scala.concurrent.{ExecutionContext, Future}

class RestPatientLabelService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends PatientLabelService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.patientlabel._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[RichPatientLabel]] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          s"/v1/patient/$patientId/label",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(Some(pagination))))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichPatientLabel]](response)
    } yield {
      reply
    }
  }

  def getDefiningCriteriaList(patientId: UuidId[Patient],
                              hypothesisId: UuidId[Hypothesis],
                              pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]
  ): Future[ListResponse[PatientLabel]] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri, s"/patient/$patientId/hypothesis", paginationQuery(Some(pagination))))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[PatientLabel]](response)
    } yield {
      reply
    }
  }

  def getByLabelIdOfPatient(patientId: UuidId[Patient], labelId: LongId[Label])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientLabel] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/label/$labelId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[RichPatientLabel](response)
    } yield {
      entity
    }
  }

  def update(origPatientLabel: PatientLabel, draftPatientLabel: PatientLabel)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientLabel] = {
    for {
      entity <- Marshal(draftPatientLabel).to[RequestEntity]
      request = HttpRequest(
        HttpMethods.PATCH,
        endpointUri(baseUri, s"/v1/patient/${origPatientLabel.patientId}/label/${origPatientLabel.labelId}"))
        .withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[RichPatientLabel](response)
    } yield {
      entity
    }
  }

}
