package xyz.driver.pdsuidomain.services.rest

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import spray.json.RootJsonReader
import xyz.driver.core.rest.{AuthorizedServiceRequestContext, ServiceTransport}
import xyz.driver.entities.users
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.{Trial, TrialIssue}
import xyz.driver.pdsuidomain.services.TrialIssueService

import scala.concurrent.ExecutionContext

class RestTrialIssueService(transport: ServiceTransport, baseUri: Uri)
                           (implicit
                            protected val materializer: Materializer,
                            protected val exec: ExecutionContext)
  extends TrialIssueService with RestHelper{

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.trialissue._
  import xyz.driver.pdsuidomain.services.TrialIssueService._

  override def create(draft: TrialIssue)
                     (implicit requestContext: AuthorizedServiceRequestContext[users.AuthUserInfo]) = {
    val trialId = draft.trialId

    implicit val jsonReader: RootJsonReader[TrialIssue] = trialIssueReader(trialId)

    for {
      entity <- Marshal(draft).to[RequestEntity]
      request = HttpRequest(HttpMethods.POST, endpointUri(baseUri, s"/v1/trial/$trialId/issue")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[TrialIssue](response)
    } yield {
      CreateReply.Created(reply)
    }
  }

  override def getListByTrialId(trialId: StringId[Trial],
                                filter: SearchFilterExpr,
                                sorting: Option[Sorting],
                                pagination: Option[Pagination])
                               (implicit requestContext: AuthorizedServiceRequestContext[users.AuthUserInfo]) = {
    implicit val jsonReader: RootJsonReader[TrialIssue] = trialIssueReader(trialId)

    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$trialId/issue",
      filterQuery(filter) ++ sortingQuery(sorting) ++ paginationQuery(pagination))
    )
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[TrialIssue]](response)
    } yield {
      GetListByTrialIdReply.EntityList(reply.items, reply.meta.itemsCount, reply.meta.lastUpdate)
    }
  }

  override def getById(trialId: StringId[Trial], id: LongId[TrialIssue])
                      (implicit requestContext: AuthorizedServiceRequestContext[users.AuthUserInfo]) = {
    implicit val jsonReader: RootJsonReader[TrialIssue] = trialIssueReader(trialId)

    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, s"/v1/trial/$trialId/issue/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[TrialIssue](response)
    } yield {
      GetByIdReply.Entity(reply)
    }
  }

  override def update(orig: TrialIssue, draft: TrialIssue)
                     (implicit requestContext: AuthorizedServiceRequestContext[users.AuthUserInfo]) = {
    val trialId = draft.trialId
    val id = orig.id.id

    implicit val jsonReader: RootJsonReader[TrialIssue] = trialIssueReader(trialId)

    for {
      entity <- Marshal(draft).to[RequestEntity]
      request = HttpRequest(HttpMethods.PATCH, endpointUri(baseUri, s"/v1/trial/$trialId/issue/$id")).withEntity(entity)
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[TrialIssue](response)
    } yield {
      UpdateReply.Updated(reply)
    }
  }

  override def delete(trialId: StringId[Trial], id: LongId[TrialIssue])
                     (implicit requestContext: AuthorizedServiceRequestContext[users.AuthUserInfo]) = {
    val request = HttpRequest(HttpMethods.DELETE, endpointUri(baseUri, s"/v1/trial/$trialId/issue/$id"))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      _        <- apiResponse[HttpEntity](response)
    } yield {
      DeleteReply.Deleted
    }
  }

}
