package xyz.driver.restquery.query

sealed trait SearchFilterNAryOperation

object SearchFilterNAryOperation {

  case object In    extends SearchFilterNAryOperation
  case object NotIn extends SearchFilterNAryOperation

  val All: Set[SearchFilterNAryOperation] = Set(
    In,
    NotIn
  )

  val nAryOperationToName: Map[SearchFilterNAryOperation, String] =
    All.map(a => a -> a.toString.toLowerCase).toMap

  val nAryOperationsFromString: Map[String, SearchFilterNAryOperation] =
    for ((k, v) <- nAryOperationToName) yield (v, k)

}
