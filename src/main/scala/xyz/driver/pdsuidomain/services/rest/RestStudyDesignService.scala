package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import xyz.driver.core.rest._
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuidomain.formats.json.ListResponse
import xyz.driver.pdsuidomain.formats.json.studydesign.ApiStudyDesign
import xyz.driver.pdsuidomain.services.StudyDesignService

class RestStudyDesignService(transport: ServiceTransport, baseUri: Uri)(
        implicit protected val materializer: ActorMaterializer,
        protected val exec: ExecutionContext)
    extends StudyDesignService with RestHelper {

  import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
  import xyz.driver.pdsuidomain.services.StudyDesignService._

  // GET               /v1/study-design           xyz.driver.server.controllers.StudyDesignController.getList

  def getAll(sorting: Option[Sorting] = None)(
          implicit requestContext: AuthenticatedRequestContext): Future[GetListReply] = {
    val request = HttpRequest(HttpMethods.GET, endpointUri(baseUri, "/v1/study-design", sortingQuery(sorting)))
    for {
      response <- transport.sendRequestGetResponse(requestContext)(request)
      reply <- apiResponse[ListResponse[ApiStudyDesign], GetListReply](response) { api =>
                GetListReply.EntityList(api.items.map(_.toDomain), api.meta.itemsCount)
              }()
    } yield {
      reply
    }
  }

}
