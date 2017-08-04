package xyz.driver.pdsuicommon.http

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.error._
import xyz.driver.pdsuicommon.error.DomainError._
import xyz.driver.pdsuicommon.error.ErrorsResponse.ResponseError
import xyz.driver.pdsuicommon.parsers._
import xyz.driver.pdsuicommon.db.{Pagination, Sorting, SearchFilterExpr}
import scala.util._

trait Directives {

  val paginated: Directive1[Pagination] = parameters(('pageSize.as[Int], 'pageNumber.as[Int])).tmap {
    case (size, number) => Pagination(size, number)
  }

  def sorted(validDimensions: Set[String]): Directive1[Sorting] = parameterSeq.flatMap { params =>
    SortingParser.parse(validDimensions, params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex)      => failWith(ex)
    }
  }

  val searchFiltered: Directive1[SearchFilterExpr] = parameterSeq.flatMap { params =>
    SearchFilterParser.parse(params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex)      => failWith(ex)
    }
  }

  @annotation.implicitNotFound("An ApiExtractor is required to complete service replies.")
  trait ApiExtractor[Reply, Api] extends PartialFunction[Reply, Api]
  object ApiExtractor {
    // Note: make sure the Reply here is the most common response
    // type. The specific entity type should be handled in the partial
    // function. E.g. `apply[GetByIdReply, Api]{case
    // GetByIdReply.Entity => Api}`
    def apply[Reply, Api](pf: PartialFunction[Reply, Api]): ApiExtractor[Reply, Api] = new ApiExtractor[Reply, Api] {
      override def isDefinedAt(x: Reply) = pf.isDefinedAt(x)
      override def apply(x: Reply)       = pf.apply(x)
    }
  }

  def completeService[Reply, Api](reply: => Reply)(implicit requestId: RequestId,
                                                   apiExtractor: ApiExtractor[Reply, Api],
                                                   apiMarshaller: ToEntityMarshaller[Api],
                                                   errorMarshaller: ToEntityMarshaller[ErrorsResponse]): Route = {

    def errorResponse(err: DomainError) =
      ErrorsResponse(Seq(ResponseError(None, err.getMessage, ErrorCode.Unspecified)), requestId)

    // TODO: rather than completing the bad requests here, we should
    // consider throwing a corresponding exception and then handling
    // it in an error handler
    reply match {
      case apiReply if apiExtractor.isDefinedAt(apiReply) =>
        complete(apiExtractor(reply))
      case err: NotFoundError =>
        complete(401 -> errorResponse(err))
      case err: AuthenticationError =>
        complete(401 -> errorResponse(err))
      case err: AuthorizationError =>
        complete(403 -> errorResponse(err))
      case err: DomainError =>
        complete(400 -> errorResponse(err))
      case other =>
        val msg = s"Got unexpected response type in completion directive: ${other.getClass.getSimpleName}"
        val res = ErrorsResponse(Seq(ResponseError(None, msg, ErrorCode.Unspecified)), requestId)
        complete(500 -> res)
    }
  }

  import xyz.driver.core.rest.AuthorizedServiceRequestContext
  import xyz.driver.core.rest.ContextHeaders
  import xyz.driver.entities.users.UserInfo

  implicit def authContext(core: AuthorizedServiceRequestContext[UserInfo]): AuthenticatedRequestContext =
    new AuthenticatedRequestContext(
      core.authenticatedUser,
      RequestId(),
      core.contextHeaders(ContextHeaders.AuthenticationTokenHeader)
    )

}

object Directives extends Directives
