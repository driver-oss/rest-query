package xyz.driver.restquery.rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import xyz.driver.restquery.query.{SearchFilterExpr, _}
import xyz.driver.restquery.rest.parsers._

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

}

object Directives extends Directives
