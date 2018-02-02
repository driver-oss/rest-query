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

  val All: Set[SearchFilterBinaryOperation] = Set(
    Eq,
    NotEq,
    Like,
    Gt,
    GtEq,
    Lt,
    LtEq
  )

  val binaryOperationToName: Map[SearchFilterBinaryOperation, String] =
    All.map(a => a -> a.toString.toLowerCase).toMap

  val binaryOperationsFromString: Map[String, SearchFilterBinaryOperation] =
    for ((k, v) <- binaryOperationToName) yield (v, k)

}
