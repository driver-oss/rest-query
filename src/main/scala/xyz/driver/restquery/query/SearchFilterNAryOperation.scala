package xyz.driver.restquery.query

sealed trait SearchFilterNAryOperation

object SearchFilterNAryOperation {

  case object In    extends SearchFilterNAryOperation
  case object NotIn extends SearchFilterNAryOperation

}
