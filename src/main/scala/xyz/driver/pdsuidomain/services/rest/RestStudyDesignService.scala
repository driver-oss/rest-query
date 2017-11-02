package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import xyz.driver.core.rest._
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities.StudyDesign
import xyz.driver.pdsuidomain.services.StudyDesignService

class RestStudyDesignService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: Materializer,
        protected val exec: ExecutionContext)
    extends StudyDesignService with RestHelper {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import xyz.driver.pdsuidomain.formats.json.listresponse._
  import xyz.driver.pdsuidomain.formats.json.studydesign._
  import xyz.driver.pdsuidomain.services.StudyDesignService._

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthorizedServiceRequestContext[AuthUserInfo]): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, "/v1/study-design", sortingQuery(sorting)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply    <- apiResponse[ListResponse[StudyDesign]](response)
    } yield {
      GetListReply.EntityList(reply.items, reply.meta.itemsCount)
    }
  }

}
