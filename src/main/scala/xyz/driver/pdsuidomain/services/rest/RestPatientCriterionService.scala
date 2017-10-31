package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientCriterionService

import scala.concurrent.{ExecutionContext, Future}

class RestPatientCriterionService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends PatientCriterionService with RestHelper {

  import spray.json.DefaultJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.patientcriterion._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.services.PatientCriterionService._

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Option[Pagination] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          s"/v1/patient/$patientId/criterion",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichPatientCriterion]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  def getById(patientId: UuidId[Patient], id: LongId[PatientCriterion])(
          implicit requestContext: AuthenticatedRequestContext): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/criterion/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientCriterion](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def updateList(patientId: UuidId[Patient], draftEntities: List[DraftPatientCriterion])(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    for {
      entity <- Marshal(draftEntities).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/patient/$patientId/criterion"))
        .withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientCriterion](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  def update(origEntity: PatientCriterion, draftEntity: PatientCriterion, patientId: UuidId[Patient])(
          implicit requestContext: AuthenticatedRequestContext): Future[UpdateReply] = {
    for {
      entity <- Marshal(draftEntity).to[RequestEntity]
      request = HttpRequest(
        HttpMethods.PATCH,
        endpointUri(baseUri, s"/v1/patient/$patientId/criterion/${origEntity.criterionId}")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientCriterion](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

}
