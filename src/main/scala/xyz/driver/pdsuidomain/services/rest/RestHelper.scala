package xyz.driver.pdsuidomain.services.rest

import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import xyz.driver.core.rest.errors.{InvalidActionException, InvalidInputException, ResourceNotFoundException}
import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation, Sorting, SortingOrder}
import xyz.driver.pdsuicommon.error._

trait RestHelper {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import ErrorsResponse._

  implicit protected val materializer: Materializer
  implicit protected val exec: ExecutionContext

  protected def endpointUri(baseUri: Uri, path: String) =
    baseUri.withPath(Uri.Path(path))

  protected def endpointUri(baseUri: Uri, path: String, query: Seq[(String, String)]) =
    baseUri.withPath(Uri.Path(path)).withQuery(Uri.Query(query: _*))

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
        Seq("filters" -> s"${dimension.tableName.fold("")(t => s"$t.") + dimension.name} ${opToString(op)} $value")
      case SearchFilterExpr.Atom.NAry(dimension, SearchFilterNAryOperation.In, values) =>
        Seq("filters" -> s"${dimension.tableName.fold("")(t => s"$t.") + dimension.name} in ${values.mkString(",")}")
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
        "pageSize"   -> pp.pageSize.toString
      )
  }

  /** Utility method to parse responses from records-acquisition-server.
    *
    * Non-2xx HTTP error codes will be cause the returned future to fail with a corresponding
    * `DomainException`.
    * @tparam ApiReply The type of the serialized reply object, contained in the HTTP entity
    * @param response The HTTP response to parse.
    * @param unmarshaller An unmarshaller that converts a successful response to an api reply.
    */
  def apiResponse[ApiReply](response: HttpResponse)(
          implicit unmarshaller: Unmarshaller[ResponseEntity, ApiReply]): Future[ApiReply] = {

    def extractErrorMessage(response: HttpResponse): Future[String] = {
      Unmarshal(response.entity)
        .to[ErrorsResponse]
        .transform(
          response => response.errors.map(_.message).mkString(", "),
          ex => InvalidInputException(s"Response has invalid format: ${ex.getMessage}")
        )
    }

    if (response.status.isSuccess) {
      Unmarshal(response.entity).to[ApiReply]
    } else {
      extractErrorMessage(response).flatMap { message =>
        Future.failed(response.status match {
          case StatusCodes.Forbidden    => InvalidActionException(message)
          case StatusCodes.NotFound     => ResourceNotFoundException(message)
          case other =>
            InvalidInputException(s"Unhandled domain error for HTTP status ${other.value}. $message")
        })
      }
    }
  }
}
