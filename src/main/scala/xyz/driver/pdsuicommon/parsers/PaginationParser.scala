package xyz.driver.server.parsers

import xyz.driver.pdsuicommon.db.Pagination
import xyz.driver.server.parsers.errors.ParseQueryArgException
import play.api.data.validation._
import play.api.routing.sird.QueryString
import xyz.driver.pdsuicommon.validation.AdditionalConstraints

import scala.util.Try

object PaginationParser {

  private val oneQueryArgConstraint: Constraint[Seq[String]] = {
    Constraint("query.oneArg") {
      case Nil      => Valid
      case x +: Nil => Valid
      case xs =>
        Invalid(new ValidationError(Seq(s"must be one argument, but there are multiple: '${xs.mkString(", ")}'")))
    }
  }

  private val pageSizeCheckConstraint: Constraint[Seq[String]] = {
    Constraint("pagination.pageSize") { args =>
      oneQueryArgConstraint(args) match {
        case x: Invalid => x
        case Valid      => AdditionalConstraints.positivePrintedNumber(args.head)
      }
    }
  }

  private val pageNumberCheckConstraint: Constraint[Seq[String]] = {
    Constraint("pagination.pageNumber") { args =>
      oneQueryArgConstraint(args) match {
        case x: Invalid => x
        case Valid      => AdditionalConstraints.positivePrintedNumber(args.head)
      }
    }
  }

  def parse(queryString: QueryString): Try[Pagination] = Try {
    val rawPageSizes   = queryString.getOrElse("pageSize", Seq(Pagination.Default.pageSize.toString))
    val rawPageNumbers = queryString.getOrElse("pageNumber", Seq(Pagination.Default.pageNumber.toString))

    val validation = Seq(
      "pageSize"   -> pageSizeCheckConstraint(rawPageSizes),
      "pageNumber" -> pageNumberCheckConstraint(rawPageNumbers)
    )

    val validationErrors = validation.collect {
      case (fieldName, e: Invalid) => (fieldName, e.errors.mkString("; "))
    }

    if (validationErrors.isEmpty) {
      Pagination(Integer.parseInt(rawPageSizes.head), Integer.parseInt(rawPageNumbers.head))
    } else {
      throw new ParseQueryArgException(validationErrors: _*)
    }
  }

}
