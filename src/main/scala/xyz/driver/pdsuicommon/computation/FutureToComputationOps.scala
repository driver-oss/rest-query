package xyz.driver.pdsuicommon.computation

import xyz.driver.pdsuicommon.error.DomainError

import scala.concurrent.{ExecutionContext, Future}

final class FutureToComputationOps[T](val self: Future[T]) extends AnyVal {

  def handleDomainError[U, ER](f: PartialFunction[T, U])(implicit unsuitableToErrorsResponse: DomainError => ER,
                                                         ec: ExecutionContext): Future[Either[ER, U]] = {
    self.map {
      case x if f.isDefinedAt(x) => Right(f(x))
      case x: DomainError        => Left(unsuitableToErrorsResponse(x))
      case x                     => throw new RuntimeException(s"Can not process $x")
    }
  }

  def toComputation[U, ER](f: PartialFunction[T, U])(implicit unsuitableToErrorsResponse: DomainError => ER,
                                                     ec: ExecutionContext): Computation[ER, U] = {
    Computation(handleDomainError(f))
  }
}
