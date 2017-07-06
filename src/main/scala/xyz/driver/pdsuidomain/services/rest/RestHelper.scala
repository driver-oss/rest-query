package xyz.driver.pdsuidomain.services.rest

import java.lang.RuntimeException

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, ResponseEntity, StatusCode, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import xyz.driver.core.rest.ServiceRequestContext
import xyz.driver.pdsuicommon.auth.{AnonymousRequestContext, AuthenticatedRequestContext}
import xyz.driver.pdsuicommon.db.{
  Pagination,
  SearchFilterBinaryOperation,
  SearchFilterExpr,
  SearchFilterNAryOperation,
  Sorting,
  SortingOrder
}
import xyz.driver.pdsuicommon.error.DomainError

trait RestHelper {

  implicit protected val materializer: Materializer
  implicit protected val exec: ExecutionContext

  protected def endpointUri(baseUri: Uri, path: String) =
    baseUri.withPath(Uri.Path(path))

  protected def endpointUri(baseUri: Uri, path: String, query: Seq[(String, String)]) =
    baseUri.withPath(Uri.Path(path)).withQuery(Uri.Query(query: _*))

  def get(baseUri: Uri, path: String, query: Seq[(String, String)] = Seq.empty): HttpRequest =
    HttpRequest(HttpMethods.GET, endpointUri(baseUri, path, query))

  def sortingQuery(sorting: Option[Sorting]): Seq[(String, String)] = {
    def dimensionQuery(dimension: Sorting.Dimension) = {
      val ord = dimension.order match {
        case SortingOrder.Ascending  => ""
        case SortingOrder.Descending => "-"
      }
      s"$ord${dimension.name}"
    }

    sorting match {
      case None                                 => Seq.empty
      case Some(dimension: Sorting.Dimension)   => Seq("sort" -> dimensionQuery(dimension))
      case Some(Sorting.Sequential(dimensions)) => Seq("sort" -> dimensions.map(dimensionQuery).mkString(","))
    }
  }

  def filterQuery(expr: SearchFilterExpr): Seq[(String, String)] = {
    def opToString(op: SearchFilterBinaryOperation) = op match {
      case SearchFilterBinaryOperation.Eq    => "eq"
      case SearchFilterBinaryOperation.NotEq => "ne"
      case SearchFilterBinaryOperation.Like  => "like"
      case SearchFilterBinaryOperation.Gt    => "gt"
      case SearchFilterBinaryOperation.GtEq  => "ge"
      case SearchFilterBinaryOperation.Lt    => "lt"
      case SearchFilterBinaryOperation.LtEq  => "le"
    }

    def exprToQuery(expr: SearchFilterExpr): Seq[(String, String)] = expr match {
      case SearchFilterExpr.Empty => Seq.empty
      case SearchFilterExpr.Atom.Binary(dimension, op, value) =>
        Seq("filters" -> s"${dimension.name} ${opToString(op)} $value")
      case SearchFilterExpr.Atom.NAry(dimension, SearchFilterNAryOperation.In, values) =>
        Seq("filters" -> s"${dimension.name} in ${values.mkString(",")}")
      case SearchFilterExpr.Intersection(ops) =>
        ops.flatMap(op => exprToQuery(op))
      case expr => sys.error(s"No parser available for filter expression $expr.")
    }

    exprToQuery(expr)
  }

  def paginationQuery(pagination: Option[Pagination]): Seq[(String, String)] = pagination match {
    case None => Seq.empty
    case Some(pp) =>
      Seq(
        "pageNumber" -> pp.pageNumber.toString,
        "pageSize"   -> pp.pageSize.toHexString
      )
  }

  /** Utility method to parse responses that encode success and errors as subtypes
    * of a common reply type.
    *
    * @tparam ApiReply The type of the serialized reply object, contained in the HTTP entity
    * @tparam DomainReply The type of the domain object that will be created from a successful reply.
    *
    * @param response The HTTP response to parse.
    * @param successMapper Transformation function from a deserialized api entity to a domain object.
    * @param errorMapper Transformation function from general domain errors to
    * specialized errors of the given DomainReply. Note that if a domain error
    * is not explicitly handled, it will be encoded as a failure in the returned future.
    * @param unmarshaller An unmarshaller that converts a successful response to an api reply.
    */
  def apiResponse[ApiReply, DomainReply](response: HttpResponse)(successMapper: ApiReply => DomainReply)(
          errorMapper: PartialFunction[DomainError, DomainReply] = PartialFunction.empty)(
          implicit unmarshaller: Unmarshaller[ResponseEntity, ApiReply]): Future[DomainReply] = {

    val domainErrors: Map[StatusCode, DomainError] = Map(
      StatusCodes.Unauthorized -> new DomainError.AuthenticationError {
        override protected def userMessage: String = "unauthorized"
      }, // 401
      StatusCodes.Forbidden -> new DomainError.AuthorizationError {
        override protected def userMessage: String = "forbidden"
      }, // 403
      StatusCodes.NotFound -> new DomainError.NotFoundError {
        override protected def userMessage: String = "not found"
      } // 404
    )

    if (response.status.isSuccess) {
      val reply = Unmarshal(response.entity).to[ApiReply]
      reply.map(successMapper)
    } else {
      val domainError = domainErrors.get(response.status)
      domainError.flatMap(errorMapper.lift) match {
        case Some(error) => Future.successful(error)
        case None =>
          Future.failed(
            new RuntimeException(
              s"Unhandled domain error for HTTP status ${response.status}. Message ${response.entity}")
          )
      }
    }
  }

  implicit def toServiceRequestContext(requestContext: AnonymousRequestContext): ServiceRequestContext = {
    val auth: Map[String, String] = requestContext match {
      case ctx: AuthenticatedRequestContext => Map("Auth-token" -> ctx.authToken)
      case _                                => Map()
    }
    new ServiceRequestContext(contextHeaders = auth)
  }

}
