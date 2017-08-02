package xyz.driver.server.parsers

import xyz.driver.pdsuicommon.db.{Pagination, SearchFilterExpr, Sorting}
import play.api.mvc._

import scala.util.Try

final case class ListRequestParameters(filter: SearchFilterExpr, sorting: Sorting, pagination: Pagination)

class ListRequestParser(validSortingFields: Set[String]) {

  def tryParse(request: Request[AnyContent]): Try[ListRequestParameters] = {
    for {
      queryFilters <- SearchFilterParser.parse(request.queryString)
      sorting      <- SortingParser.parse(validSortingFields, request.queryString)
      pagination   <- PaginationParser.parse(request.queryString)
    } yield ListRequestParameters(queryFilters, sorting, pagination)
  }

}
