package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json._
import xyz.driver.pdsuidomain.entities._

object patienthistory {
  import DefaultJsonProtocol._
  import PatientHistory._
  import common._

  implicit val patientStateFormat: RootJsonFormat[State] = new EnumJsonFormat[State](
    "Verify" -> State.Verify,
    "Curate" -> State.Curate,
    "Review" -> State.Review,
    "Flag"   -> State.Flag
  )

  implicit val patientActionFormat: RootJsonFormat[Action] = new EnumJsonFormat[Action](
    "Start"    -> Action.Start,
    "Submit"   -> Action.Submit,
    "Unassign" -> Action.Unassign,
    "Resolve"  -> Action.Resolve,
    "Flag"     -> Action.Flag,
    "Archive"  -> Action.Archive
  )

  implicit val patientHistoryFormat: RootJsonFormat[PatientHistory] = jsonFormat6(PatientHistory.apply)

}
