package xyz.driver.pdsuicommon.computation

import scala.concurrent.{ExecutionContext, Future}

/**
  * Takes care of computations
  *
  * Success(either) - the computation will be continued.
  * Failure(error) - the computation was failed with unhandled error.
  *
  * Either[Result, T]:
  * Left(result) is a final and handled result, another computations (map, flatMap) will be ignored.
  * Right(T) is a current result. Functions in map/flatMap will continue the computation.
  *
  * Example:
  * {{{
  * import scala.concurrent.ExecutionContext.Implicits.global
  * import scala.concurrent.{ExecutionContext, Future}
  * import xyz.driver.pdsuicommon.computation.Computation
  *
  * def successful = for {
  *   x <- Computation.continue(1)
  *   y <- Computation.continue(2)
  * } yield s"\$x + \$y"
  *
  * // Prints "Success(1 + 2)"
  * successful.join.onComplete(print)
  *
  * def failed = for {
  *   x <- Computation.abort("Failed on x")
  *   _ = print("Second step")
  *   y <- Computation.continue(2)
  * } yield s"\$x + \$y"
  *
  * // Prints "Success(Failed on x)"
  * failed.join.onComplete(print)
  * }}}
  *
  * TODO: Make future private
  *
  * @param future The final flow in a future.
  * @tparam R Type of result for aborted computation.
  * @tparam T Type of result for continued computation.
  */
final case class Computation[+R, +T](future: Future[Either[R, T]]) {

  def flatMap[R2, T2](f: T => Computation[R2, T2])(implicit ec: ExecutionContext, ev: R <:< R2): Computation[R2, T2] = {
    Computation(future.flatMap {
      case Left(x)  => Future.successful(Left(x))
      case Right(x) => f(x).future
    })
  }

  def processExceptions[R2](f: PartialFunction[Throwable, R2])(implicit ev1: R <:< R2,
                                                               ec: ExecutionContext): Computation[R2, T] = {
    val strategy = f.andThen(x => Left(x): Either[R2, T])
    val castedFuture: Future[Either[R2, T]] = future.map {
      case Left(x)  => Left(x)
      case Right(x) => Right(x)
    }
    Computation(castedFuture.recover(strategy))
  }

  def map[T2](f: T => T2)(implicit ec: ExecutionContext): Computation[R, T2] = flatMap { a =>
    Computation.continue(f(a))
  }

  def mapLeft[R2](f: R => R2)(implicit ec: ExecutionContext): Computation[R2, T] = {
    Computation(future.map {
      case Left(x)  => Left(f(x))
      case Right(x) => Right(x)
    })
  }

  def mapAll[R2, T2](onLeft: R => Computation[R2, T2])(onRight: T => Computation[R2, T2])(
          onFailure: () => Computation[R2, T2])(implicit ec: ExecutionContext): Computation[R2, T2] = {

    Computation(future.flatMap { success =>
      if (success.isRight) onRight(success.right.get).future
      else onLeft(success.left.get).future
    } recoverWith {
      case _ => onFailure().future
    })
  }

  def andThen(f: T => Any)(implicit ec: ExecutionContext): Computation[R, T] = map { a =>
    f(a)
    a
  }

  def filter(f: T => Boolean)(implicit ec: ExecutionContext): Computation[R, T] = map { a =>
    if (f(a)) a
    else throw new NoSuchElementException("When filtering")
  }

  def withFilter(f: T => Boolean)(implicit ec: ExecutionContext): Computation[R, T] = filter(f)

  def foreach[T2](f: T => T2)(implicit ec: ExecutionContext): Unit = future.foreach {
    case Right(x) => f(x)
    case _        =>
  }

  def toFuture[R2](resultFormatter: T => R2)(implicit ec: ExecutionContext, ev: R <:< R2): Future[R2] = future.map {
    case Left(x)  => x
    case Right(x) => resultFormatter(x)
  }

  def toFuture[R2](implicit ec: ExecutionContext, ev1: R <:< R2, ev2: T <:< R2): Future[R2] = future.map {
    case Left(x)  => x
    case Right(x) => x
  }
}

object Computation {

  def continue[T](x: T): Computation[Nothing, T] = Computation(Future.successful(Right(x)))

  def abort[R](result: R): Computation[R, Nothing] = Computation(Future.successful(Left(result)))

  def fail(exception: Throwable): Computation[Nothing, Nothing] = Computation(Future.failed(exception))

  def fromFuture[T](input: Future[T])(implicit ec: ExecutionContext): Computation[Nothing, T] = Computation {
    input.map { x =>
      Right(x)
    }
  }
}
