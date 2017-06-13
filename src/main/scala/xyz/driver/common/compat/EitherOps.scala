package xyz.driver.common.compat

final class EitherOps[A, B](val self: Either[A, B]) extends AnyVal {

  def map[B2](f: B => B2): Either[A, B2] = flatMap { x => Right(f(x)) }

  def flatMap[B2](f: B => Either[A, B2]): Either[A, B2] = self match {
    case Left(x) => Left(x)
    case Right(x) => f(x)
  }

}
