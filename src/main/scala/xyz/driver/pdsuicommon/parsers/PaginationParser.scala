package xyz.driver.pdsuicommon.parsers

import xyz.driver.pdsuicommon.db._
import scala.util._

object PaginationParser {

  @deprecated("play-akka transition", "0")
  def parse(query: Map[String, Seq[String]]): Try[Pagination] =
    parse(query.toSeq.flatMap {
      case (key, values) =>
        values.map(value => key -> value)
    })

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
