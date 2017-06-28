package xyz.driver.pdsuicommon.validation

import org.davidbild.tristate.Tristate
import play.api.data.validation._

object AdditionalConstraints {

  val optionNonEmptyConstraint: Constraint[Option[Any]] = {
    Constraint("option.nonEmpty") {
      case Some(x) => Valid
      case None => Invalid("is empty")
    }
  }

  val tristateSpecifiedConstraint: Constraint[Tristate[Any]] = {
    Constraint("tristate.specified") {
      case Tristate.Unspecified => Invalid("unspecified")
      case _ => Valid
    }
  }

  val uuid: Constraint[String] = {
    Constraints.pattern("""[\da-z]{8}-[\da-z]{4}-[\da-z]{4}-[\da-z]{4}-[\da-z]{12}""".r, "uuid", "invalid uuid")
  }
}
