package xyz.driver.restquery.http.parsers

import xyz.driver.restquery.domain.Pagination

import scala.util._

object PaginationParser {

  def parse(query: Seq[(String, String)]): Try[Pagination] = {
    val IntString = """(\d+)""".r
    def validate(field: String, default: Int) = query.collectFirst { case (`field`, size) => size } match {
      case Some(IntString(x)) if x.toInt > 0 => x.toInt
      case Some(IntString(x))                => throw new ParseQueryArgException((field, s"must greater than zero (found $x)"))
      case Some(str)                         => throw new ParseQueryArgException((field, s"must be an integer (found $str)"))
      case None                              => default
    }

    Try {
      Pagination(validate("pageSize", Pagination.Default.pageSize),
                 validate("pageNumber", Pagination.Default.pageNumber))
    }
  }
}
