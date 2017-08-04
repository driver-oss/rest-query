package xyz.driver.pdsuicommon.http

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model._
import xyz.driver.core.rest.AuthorizedServiceRequestContext
import xyz.driver.core.rest.ContextHeaders
import xyz.driver.entities.users.UserInfo
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.error._
import xyz.driver.pdsuicommon.error.DomainError._
import xyz.driver.pdsuicommon.error.ErrorsResponse.ResponseError
import xyz.driver.pdsuicommon.parsers._
import xyz.driver.pdsuicommon.db.{Pagination, Sorting, SearchFilterExpr}

import scala.util._
import scala.concurrent._

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

  @annotation.implicitNotFound("An ApiExtractor of ${Reply} to ${Api} is required to complete service replies.")
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

  implicit def replyMarshaller[Reply, Api](
          implicit ctx: AuthenticatedRequestContext,
          apiExtractor: ApiExtractor[Reply, Api],
          apiMarshaller: ToEntityMarshaller[Api],
          errorMarshaller: ToEntityMarshaller[ErrorsResponse]
  ): ToResponseMarshaller[Reply] = {

    def errorResponse(err: DomainError) =
      ErrorsResponse(Seq(ResponseError(None, err.getMessage, ErrorCode.Unspecified)), ctx.requestId)

    Marshaller[Reply, HttpResponse] { (executionContext: ExecutionContext) => (reply: Reply) =>
      implicit val ec = executionContext
      reply match {
        case apiReply if apiExtractor.isDefinedAt(apiReply) =>
          Marshaller.fromToEntityMarshaller[Api](StatusCodes.OK).apply(apiExtractor(apiReply))
        case err: NotFoundError =>
          Marshaller.fromToEntityMarshaller[ErrorsResponse](StatusCodes.Unauthorized).apply(errorResponse(err))
        case err: AuthorizationError =>
          Marshaller.fromToEntityMarshaller[ErrorsResponse](StatusCodes.Forbidden).apply(errorResponse(err))
        case err: DomainError =>
          Marshaller.fromToEntityMarshaller[ErrorsResponse](StatusCodes.BadRequest).apply(errorResponse(err))
        case other =>
          val msg = s"Got unexpected response type in completion directive: ${other.getClass.getSimpleName}"
          val res = ErrorsResponse(Seq(ResponseError(None, msg, ErrorCode.Unspecified)), ctx.requestId)
          Marshaller.fromToEntityMarshaller[ErrorsResponse](StatusCodes.InternalServerError).apply(res)
      }
    }
  }

  implicit class PdsContext(core: AuthorizedServiceRequestContext[UserInfo]) {
    def authenticated = new AuthenticatedRequestContext(
      core.authenticatedUser,
      RequestId(),
      core.contextHeaders(ContextHeaders.AuthenticationTokenHeader)
    )
  }

}

object Directives extends Directives
