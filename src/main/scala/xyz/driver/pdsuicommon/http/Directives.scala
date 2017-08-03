package xyz.driver.pdsuicommon.http

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import xyz.driver.pdsuicommon.parsers._
import xyz.driver.pdsuicommon.db.{Pagination, Sorting, SearchFilterExpr}
import scala.util._

trait Directives {

  val paginated: Directive1[Pagination] = parameters(('pageSize.as[Int], 'pageNumber.as[Int])).tmap {
    case (size, number) => Pagination(size, number)
  }

  def sorted(validDimensions: Set[String]): Directive1[Sorting] = parameterSeq.flatMap{ params =>
    SortingParser.parse(validDimensions, params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex) => failWith(ex)
    }
  }

  val searchFiltered: Directive1[SearchFilterExpr] = parameterSeq.flatMap{ params =>
    SearchFilterParser.parse(params) match {
      case Success(sorting) => provide(sorting)
      case Failure(ex) => failWith(ex)
    }
  }

}

object Directives extends Directives
