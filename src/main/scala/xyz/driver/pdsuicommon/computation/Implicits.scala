package xyz.driver.pdsuicommon.computation

import scala.concurrent.Future
import scala.util.Try

trait Implicits {

  implicit def futureToFutureComputationOps[T](self: Future[T]): FutureToComputationOps[T] = {
    new FutureToComputationOps[T](self)
  }

  implicit def tryToTryComputationOps[T](self: Try[T]): TryToComputationOps[T] = {
    new TryToComputationOps[T](self)
  }
}
