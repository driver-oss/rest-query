package xyz.driver.pdsuicommon.computation

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

final class TryToComputationOps[T](val self: Try[T]) extends AnyVal {

  def toComputation[ER](implicit exceptionToErrorResponse: Throwable => ER, ec: ExecutionContext): Computation[ER, T] =
    self match {
      case Success(x)           => Computation.continue(x)
      case Failure(NonFatal(e)) => Computation.abort(exceptionToErrorResponse(e))
      case Failure(e)           => Computation.fail(e)
    }
}
