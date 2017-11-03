package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json._
import xyz.driver.pdsuidomain.entities._

object recordhistory {
  import DefaultJsonProtocol._
  import MedicalRecordHistory._
  import common._

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
