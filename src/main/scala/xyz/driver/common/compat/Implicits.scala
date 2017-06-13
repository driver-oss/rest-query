package xyz.driver.common.compat

object Implicits {

  implicit def toEitherOps[A, B](self: Either[A, B]): EitherOps[A, B] = new EitherOps(self)

}
