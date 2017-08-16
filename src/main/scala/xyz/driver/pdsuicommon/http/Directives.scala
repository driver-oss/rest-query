package xyz.driver.pdsuicommon.http

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import xyz.driver.core.rest.ContextHeaders
import xyz.driver.entities.users.AuthUserInfo
import xyz.driver.pdsuicommon.auth._
import xyz.driver.pdsuicommon.error._
import xyz.driver.pdsuicommon.error.DomainError._
import xyz.driver.pdsuicommon.error.ErrorsResponse.ResponseError
import xyz.driver.pdsuicommon.parsers._
import xyz.driver.pdsuicommon.db.{Pagination, Sorting, SearchFilterExpr}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.serialization.PlayJsonSupport._
import xyz.driver.core.rest.AuthProvider

import scala.util._

trait Directives {

  val paginated: Directive1[Pagination] = parameterSeq.flatMap { params =>
    PaginationParser.parse(params) match {
      case Success(pagination) => provide(pagination)
      case Failure(ex)         => failWith(ex)
    }
  }

  def sorted(validDimensions: Set[String] = Set.empty): Directive1[Sorting] = parameterSeq.flatMap { params =>
    SortingParser.parse(validDimensions, params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex)      => failWith(ex)
    }
  }

  val dimensioned: Directive1[Dimensions] = parameterSeq.flatMap { params =>
    DimensionsParser.tryParse(params) match {
      case Success(dims) => provide(dims)
      case Failure(ex)   => failWith(ex)
    }
  }

  val searchFiltered: Directive1[SearchFilterExpr] = parameterSeq.flatMap { params =>
    SearchFilterParser.parse(params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex)      => failWith(ex)
    }
  }

  def StringIdInPath[T]: PathMatcher1[StringId[T]] =
    PathMatchers.Segment.map((id) => StringId(id.toString.toLowerCase))

  def LongIdInPath[T]: PathMatcher1[LongId[T]] =
    PathMatchers.LongNumber.map((id) => LongId(id))

  def UuidIdInPath[T]: PathMatcher1[UuidId[T]] =
    PathMatchers.JavaUUID.map((id) => UuidId(id))

  def failFast[A](reply: A): A = reply match {
    case err: NotFoundError       => throw new NotFoundException(err.getMessage)
    case err: AuthenticationError => throw new AuthenticationException(err.getMessage)
    case err: AuthorizationError  => throw new AuthorizationException(err.getMessage)
    case err: DomainError         => throw new DomainException(err.getMessage)
    case other                    => other
  }

  def domainExceptionHandler(req: RequestId) = {
    def errorResponse(err: DomainError) =
      ErrorsResponse(Seq(ResponseError(None, err.getMessage, ErrorCode.Unspecified)), req)
    ExceptionHandler {
      case err: AuthenticationError => complete(StatusCodes.Unauthorized -> errorResponse(err))
      case err: AuthorizationError  => complete(StatusCodes.Forbidden    -> errorResponse(err))
      case err: NotFoundError       => complete(StatusCodes.NotFound     -> errorResponse(err))
      case err: DomainError         => complete(StatusCodes.BadRequest   -> errorResponse(err))
    }
  }

  val tracked: Directive1[RequestId] = optionalHeaderValueByName(ContextHeaders.TrackingIdHeader) flatMap {
    case Some(id) => provide(RequestId(id))
    case None     => provide(RequestId())
  }

  val handleDomainExceptions: Directive0 = tracked.flatMap {
    case id =>
      handleExceptions(domainExceptionHandler(id))
  }

  implicit class AuthProviderWrapper(provider: AuthProvider[AuthUserInfo]) {
    val authenticate: Directive1[AuthenticatedRequestContext] = (provider.authorize() & tracked) tflatMap {
      case (core, requestId) =>
        provide(
          new AuthenticatedRequestContext(
            core.authenticatedUser,
            requestId,
            core.contextHeaders(ContextHeaders.AuthenticationTokenHeader)
          ))
    }
  }

}

object Directives extends Directives
