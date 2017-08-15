package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuidomain.entities._

object recordhistory {
  import DefaultJsonProtocol._
  import common._
  import MedicalRecordHistory._

  implicit val recordStateFormat = new EnumJsonFormat[State](
    "Clean"    -> State.Clean,
    "Organize" -> State.Organize,
    "Review"   -> State.Review,
    "Flag"     -> State.Flag
  )

  implicit val recordActionFormat = new EnumJsonFormat[Action](
    "Start"    -> Action.Start,
    "Submit"   -> Action.Submit,
    "Unassign" -> Action.Unassign,
    "Resolve"  -> Action.Resolve,
    "Flag"     -> Action.Flag,
    "Archive"  -> Action.Archive
  )

  implicit val recordHistoryFormat: RootJsonFormat[MedicalRecordHistory] = jsonFormat6(MedicalRecordHistory.apply)

}
