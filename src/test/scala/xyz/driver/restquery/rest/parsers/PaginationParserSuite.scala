package xyz.driver.restquery.http.parsers

import xyz.driver.restquery.http.parsers.TestUtils._
import org.scalatest.{FreeSpecLike, MustMatchers}
import xyz.driver.restquery.domain.Pagination

import scala.util.{Failure, Try}

class PaginationParserSuite extends FreeSpecLike with MustMatchers {

  "parse" - {
    "pageSize" - {
      "should parse positive value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "10",
            "pageNumber" -> "1"
          ))
        pagination must success
        pagination.get.pageSize mustBe 10
      }

      "should return a default value if there is no one" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageNumber" -> "1"
          ))
        pagination must success
        pagination.get.pageSize mustBe 100
      }

      "should return a error for zero value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "0",
            "pageNumber" -> "1"
          ))

        checkFailedValidationOnlyOn(pagination, "pageSize")
      }

      "should return a error for negative value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "-10",
            "pageNumber" -> "1"
          ))

        checkFailedValidationOnlyOn(pagination, "pageSize")
      }
    }

    "pageNumber" - {
      "should parse positive value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "1",
            "pageNumber" -> "1"
          ))
        pagination must success
        pagination.get.pageSize mustBe 1
      }

      "should return a default value if there is no one" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize" -> "1"
          ))
        pagination must success
        pagination.get.pageNumber mustBe 1
      }

      "should return a error for zero value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "1",
            "pageNumber" -> "0"
          ))

        checkFailedValidationOnlyOn(pagination, "pageNumber")
      }

      "should return a error for negative value" in {
        val pagination = PaginationParser.parse(
          Seq(
            "pageSize"   -> "1",
            "pageNumber" -> "-1"
          ))

        checkFailedValidationOnlyOn(pagination, "pageNumber")
      }
    }
  }

  private def checkFailedValidationOnlyOn(pagination: Try[Pagination], key: String): Unit = {
    pagination must failWith[ParseQueryArgException]

    val Failure(e: ParseQueryArgException) = pagination
    e.errors.size mustBe 1
    e.errors.head._1 mustBe key
  }

}
