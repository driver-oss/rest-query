package xyz.driver.restquery.query

sealed trait SearchFilterBinaryOperation

object SearchFilterBinaryOperation {

  case object Eq    extends SearchFilterBinaryOperation
  case object NotEq extends SearchFilterBinaryOperation
  case object Like  extends SearchFilterBinaryOperation
  case object Gt    extends SearchFilterBinaryOperation
  case object GtEq  extends SearchFilterBinaryOperation
  case object Lt    extends SearchFilterBinaryOperation
  case object LtEq  extends SearchFilterBinaryOperation

}
