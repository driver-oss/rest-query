package xyz.driver.pdsuicommon.db

import xyz.driver.pdsuicommon.logging._

/**
  * @param pageNumber Starts with 1
  */
final case class Pagination(pageSize: Int, pageNumber: Int)

object Pagination {

  // @see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-CommonRequestQueryParametersForWebServices
  val Default = Pagination(pageSize = 100, pageNumber = 1)

  implicit def toPhiString(x: Pagination): PhiString = {
    import x._
    phi"Pagination(pageSize=${Unsafe(pageSize)}, pageNumber=${Unsafe(pageNumber)})"
  }
}
