package xyz.driver.restquery.domain

/**
  * @param pageNumber Starts with 1
  */
final case class Pagination(pageSize: Int, pageNumber: Int)

object Pagination {

  // @see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-CommonRequestQueryParametersForWebServices
  val Default = Pagination(pageSize = 100, pageNumber = 1)
}
