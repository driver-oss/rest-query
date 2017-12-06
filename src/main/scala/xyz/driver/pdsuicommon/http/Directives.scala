package xyz.driver.pdsuicommon.http

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import xyz.driver.core.app.DriverApp
import xyz.driver.pdsuicommon.error._
import xyz.driver.pdsuicommon.error.DomainError._
import xyz.driver.pdsuicommon.error.ErrorsResponse.ResponseError
import xyz.driver.pdsuicommon.parsers._
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import xyz.driver.pdsuicommon.domain._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import xyz.driver.core.generators
import xyz.driver.core.rest.ContextHeaders
import xyz.driver.core.rest.errors.{InvalidActionException, InvalidInputException, ResourceNotFoundException}

import scala.util.control._
import scala.util._

trait Directives {

  val paginated: Directive1[Pagination] = parameterSeq.flatMap { params =>
    PaginationParser.parse(params) match {
      case Success(pagination) => provide(pagination)
      case Failure(ex) =>
        reject(ValidationRejection("invalid pagination parameter", Some(ex)))
    }
  }

  def sorted(validDimensions: Set[String] = Set.empty): Directive1[Sorting] = parameterSeq.flatMap { params =>
    SortingParser.parse(validDimensions, params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex) =>
        reject(ValidationRejection("invalid sorting parameter", Some(ex)))
    }
  }

  val dimensioned: Directive1[Dimensions] = parameterSeq.flatMap { params =>
    DimensionsParser.tryParse(params) match {
      case Success(dims) => provide(dims)
      case Failure(ex) =>
        reject(ValidationRejection("invalid dimension parameter", Some(ex)))
    }
  }

  val searchFiltered: Directive1[SearchFilterExpr] = parameterSeq.flatMap { params =>
    SearchFilterParser.parse(params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex) =>
        reject(ValidationRejection("invalid filter parameter", Some(ex)))
    }
  }

  def StringIdInPath[T]: PathMatcher1[StringId[T]] =
    PathMatchers.Segment.map((id) => StringId(id.toString))

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

  def domainExceptionHandler(req: String): ExceptionHandler = {
    def errorResponse(msg: String, code: Int) =
      ErrorsResponse(Seq(ResponseError(None, msg, code)), req)
    ExceptionHandler {
      case ex: AuthenticationException =>
        complete(StatusCodes.Unauthorized -> errorResponse(ex.getMessage, 401))

      case ex: InvalidActionException =>
        complete(StatusCodes.Forbidden -> errorResponse(ex.message, 403))

      case ex: AuthorizationException =>
        complete(StatusCodes.Forbidden -> errorResponse(ex.getMessage, 403))

      case ex: ResourceNotFoundException =>
        complete(StatusCodes.NotFound -> errorResponse(ex.message, 404))

      case ex: NotFoundException =>
        complete(StatusCodes.NotFound -> errorResponse(ex.getMessage, 404))

      case ex: InvalidInputException =>
        complete(StatusCodes.BadRequest -> errorResponse(ex.message, 400))

      case ex: DomainException =>
        complete(StatusCodes.BadRequest -> errorResponse(ex.getMessage, 400))

      case NonFatal(ex) =>
        complete(StatusCodes.InternalServerError -> errorResponse(ex.getMessage, 500))
    }
  }

  def domainRejectionHandler(req: String): RejectionHandler = {
    def wrapContent(message: String) = {
      import ErrorsResponse._
      val err: ErrorsResponse = ErrorsResponse(Seq(ResponseError(None, message, 1)), req)
      val text                = errorsResponseJsonFormat.write(err).toString()
      HttpEntity(ContentTypes.`application/json`, text)
    }
    DriverApp.rejectionHandler.mapRejectionResponse {
      case res @ HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
        res.copy(entity = wrapContent(ent.data.utf8String))
      case x => x // pass through all other types of responses
    }
  }

  val tracked: Directive1[String] = optionalHeaderValueByName(ContextHeaders.TrackingIdHeader) flatMap {
    case Some(id) => provide(id)
    case None     => provide(generators.nextUuid().toString)
  }

  val domainResponse: Directive0 = tracked.flatMap { id =>
    handleExceptions(domainExceptionHandler(id)) & handleRejections(domainRejectionHandler(id))
  }

}

object Directives extends Directives
