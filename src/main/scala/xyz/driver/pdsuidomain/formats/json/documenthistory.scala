package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json.EnumJsonFormat
import xyz.driver.pdsuidomain.entities._

object documenthistory {
  import DefaultJsonProtocol._
  import DocumentHistory._
  import common._

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
