package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.logging._

final case class Hypothesis(id: UuidId[Hypothesis], name: String, treatmentType: String, description: String)

object Hypothesis {
  implicit def toPhiString(x: Hypothesis): PhiString = {
    import x._
    phi"Hypothesis(id=$id, name=${Unsafe(name)})"
  }
}
