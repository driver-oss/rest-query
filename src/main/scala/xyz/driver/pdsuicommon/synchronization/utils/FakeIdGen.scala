package xyz.driver.pdsuicommon.synchronization.utils

import xyz.driver.pdsuicommon.synchronization.domain.FakeId

/**
  * Used to generate a fake id from an entity.
  * A fake id is used in comparison between entities with different types,
  * for example, RawTrial and Trial.
  *
  * @see FakeId
  */
trait FakeIdGen[-T] extends (T => FakeId) {

  def getFor(x: T): FakeId

  override def apply(x: T): FakeId = getFor(x)

}

object FakeIdGen {

  def create[T](f: T => FakeId) = new FakeIdGen[T] {
    override def getFor(x: T): FakeId = f(x)
  }

}
