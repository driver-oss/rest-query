package xyz.driver.pdsuicommon.synchronization.domain

/*
 It is like an Id for entities those haven't an Id, but should be unique.
 For example,
 RawArm has the name, the kind and the intervention fields.
 It has not an Id, but should be identified by the name field.
 So, the name field is a fake id for RawArm.
 */
final case class FakeId(value: String)

object FakeId {
  implicit val ordering: Ordering[FakeId] = Ordering.by(_.value)
}
