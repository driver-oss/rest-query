package xyz.driver.pdsuidomain.formats.json.arm

import xyz.driver.pdsuidomain.entities.Arm
import play.api.libs.json.{Format, Json}

final case class ApiPartialArm(name: String) {

  def applyTo(arm: Arm): Arm = arm.copy(name = name)
}

object ApiPartialArm {

  implicit val format: Format[ApiPartialArm] = Json.format
}
