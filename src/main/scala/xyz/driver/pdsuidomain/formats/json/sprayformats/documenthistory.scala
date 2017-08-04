package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuidomain.entities._

object documenthistory {
  import DefaultJsonProtocol._
  import common._
  import DocumentHistory._

  implicit val documentStateFormat = new EnumJsonFormat[State](
    "Extract" -> State.Extract,
    "Review"  -> State.Review,
    "Flag"    -> State.Flag
  )

  implicit val documentActionFormat = new EnumJsonFormat[Action](
    "Start"    -> Action.Start,
    "Submit"   -> Action.Submit,
    "Unassign" -> Action.Unassign,
    "Resolve"  -> Action.Resolve,
    "Flag"     -> Action.Flag,
    "Archive"  -> Action.Archive
  )

  implicit val documentHistoryFormat: RootJsonFormat[DocumentHistory] = jsonFormat6(DocumentHistory.apply)

}
