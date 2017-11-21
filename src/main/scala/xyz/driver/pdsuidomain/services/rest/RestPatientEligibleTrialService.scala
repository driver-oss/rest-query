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
import xyz.driver.pdsuidomain.services.PatientCriterionService.RichPatientCriterion
import xyz.driver.pdsuidomain.services.{PatientEligibleTrialService, PatientLabelService}

import scala.concurrent.{ExecutionContext, Future}

class RestPatientEligibleTrialService(transport: ServiceTransport, baseUri: Uri)(
  implicit protected val materializer: Materializer,
  protected val exec: ExecutionContext)
  extends PatientEligibleTrialService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.patienteligibletrial._
  import xyz.driver.pdsuidomain.formats.json.patientcriterion._
  import PatientEligibleTrialService._

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
      reply    <- apiResponse[ListResponse[RichPatientEligibleTrial]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }


  def getById(patientId: UuidId[Patient], id: LongId[PatientTrialArmGroup])(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetByIdReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/trial/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientEligibleTrial](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  def getCriterionListByGroupId(patientId: UuidId[Patient], id: LongId[PatientTrialArmGroup])(
    implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetCriterionListOfGroupReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/patient/$patientId/trial/$id/criterion"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[RichPatientCriterion]](response)
    } yield {
      GetCriterionListOfGroupReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

  def update(origEligibleTrialWithTrial: RichPatientEligibleTrial,
             draftPatientTrialArmGroup: PatientTrialArmGroupView)(
              implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[UpdateReply] = {
    for {
      entity <- Marshal(draftPatientTrialArmGroup).to[RequestEntity]
      request = HttpRequest(
        HttpMethods.PATCH,
        endpointUri(baseUri, s"/v1/patient/${origEligibleTrialWithTrial.group.patientId}/trial/${origEligibleTrialWithTrial.trial.id}"))
        .withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[RichPatientEligibleTrial](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

}
