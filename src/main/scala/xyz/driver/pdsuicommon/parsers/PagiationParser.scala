package xyz.driver.pdsuicommon.parsers

import xyz.driver.pdsuicommon.db._
import scala.util._

object PaginationParser {

  @deprecated("play-akka transition", "0")
  def parse(query: Map[String, Seq[String]]): Try[Pagination] =
    parse(query.toSeq.flatMap{ case (key, values) =>
      values.map(value => key -> value)
    })

  def parse(query: Seq[(String, String)]): Try[Pagination] = {
    val IntString = """\d+""".r
    def validate(field: String) = query.collectFirst{case (`field`, size) => size} match {
      case Some(IntString(x)) => x.toInt
      case Some(str) => throw new ParseQueryArgException((field, s"must be an integer (found $str)"))
      case None => throw new ParseQueryArgException((field, "must be defined"))
    }

    Try {
      Pagination(validate("pageSize"), validate("pageNumber"))
    }
  }
}
