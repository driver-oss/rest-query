package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuidomain.entities._

object patienthistory {
  import DefaultJsonProtocol._
  import common._
  import PatientHistory._

  implicit val patientStateFormat = new EnumJsonFormat[State](
    "Verify" -> State.Verify,
    "Curate" -> State.Curate,
    "Review" -> State.Review,
    "Flag"   -> State.Flag
  )

  implicit val patientActionFormat = new EnumJsonFormat[Action](
    "Start"    -> Action.Start,
    "Submit"   -> Action.Submit,
    "Unassign" -> Action.Unassign,
    "Resolve"  -> Action.Resolve,
    "Flag"     -> Action.Flag,
    "Archive"  -> Action.Archive
  )

  implicit val patientHistoryFormat: RootJsonFormat[PatientHistory] = jsonFormat6(PatientHistory.apply)

}
