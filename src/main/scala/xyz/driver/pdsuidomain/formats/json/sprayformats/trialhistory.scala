package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuidomain.entities._

object trialhistory {
  import DefaultJsonProtocol._
  import common._
  import TrialHistory._

  implicit val trialStateFormat = new EnumJsonFormat[State](
    "Summarize"    -> State.Summarize,
    "Criteriarize" -> State.Criteriarize,
    "Review"       -> State.Review,
    "Flag"         -> State.Flag
  )

  implicit val trialActionFormat = new EnumJsonFormat[Action](
    "Start"    -> Action.Start,
    "Submit"   -> Action.Submit,
    "Unassign" -> Action.Unassign,
    "Resolve"  -> Action.Resolve,
    "Flag"     -> Action.Flag,
    "Archive"  -> Action.Archive
  )

  implicit val trialHistoryFormat: RootJsonFormat[TrialHistory] = jsonFormat6(TrialHistory.apply)

}
