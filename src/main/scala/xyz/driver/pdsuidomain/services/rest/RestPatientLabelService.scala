package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.entities.labels.Label
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
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
  import xyz.driver.pdsuidomain.services.PatientLabelService._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          s"/v1/patient/$patientId/label",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichPatientLabel]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def getDefiningCriteriaList(patientId: UuidId[Patient],
                              hypothesisId: UuidId[Hypothesis],
                              pagination: Option[Pagination] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]
  ): Future[GetDefiningCriteriaListReply] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri, s"/patient/$patientId/hypothesis", paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[PatientLabel]](response)
    } yield {
      GetDefiningCriteriaListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def getByLabelIdOfPatient(patientId: UuidId[Patient], labelId: LongId[Label])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByLabelIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/label/$labelId"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientLabel](response)
    } yield {
      GetByLabelIdReply.Entity(reply)
    }
  }

  def update(origPatientLabel: PatientLabel, draftPatientLabel: PatientLabel)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    for {
      entity <- Marshal(draftPatientLabel).to[RequestEntity]
      request = HttpRequest(
        HttpMethods.PATCH,
        endpointUri(baseUri, s"/v1/patient/${origPatientLabel.patientId}/label/${origPatientLabel.labelId}"))
        .withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientLabel](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

}
