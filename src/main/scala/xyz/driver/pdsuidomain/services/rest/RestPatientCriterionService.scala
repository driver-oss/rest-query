package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest.{Pagination => _, _}
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db.{Pagination, _}
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

  def getAll(patientId: UuidId[Patient],
             filter: SearchFilterExpr = SearchFilterExpr.Empty,
             sorting: Option[Sorting] = None,
             pagination: Pagination = Pagination.Default)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[ListResponse[RichPatientCriterion]] = {
    val request = HttpRequest(HttpMethods.GET,
                              endpointUri(baseUri,
                                          s"/v1/patient/$patientId/criterion",
                                          filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(Some(pagination))))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichPatientCriterion]](response)
    } yield {
      reply
    }
  }

  def getById(patientId: UuidId[Patient], id: LongId[PatientCriterion])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientCriterion] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/criterion/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[RichPatientCriterion](response)
    } yield {
      entity
    }
  }

  def updateList(patientId: UuidId[Patient], draftEntities: List[DraftPatientCriterion])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[Unit] = {
    for {
      entity <- Marshal(draftEntities).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/patient/$patientId/criterion"))
        .withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[RichPatientCriterion](response)
    } yield {
      ()
    }
  }

  def update(origEntity: PatientCriterion, draftEntity: PatientCriterion, patientId: UuidId[Patient])(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[RichPatientCriterion] = {
    for {
      entity <- Marshal(draftEntity).to[RequestEntity]
      request = HttpRequest(
        HttpMethods.PATCH,
        endpointUri(baseUri, s"/v1/patient/$patientId/criterion/${origEntity.criterionId}")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      entity   <- apiResponse[RichPatientCriterion](response)
    } yield {
      entity
    }
  }

}
