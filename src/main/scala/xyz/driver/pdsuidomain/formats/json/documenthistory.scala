package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json._
import xyz.driver.pdsuidomain.entities._

object documenthistory {
  import DefaultJsonProtocol._
  import DocumentHistory._
  import common._

  implicit val documentStateFormat = new EnumJsonFormat[State](
    "New"     -> State.New,
    "Extract" -> State.Extract,
    "Done"    -> State.Done,
    "Review"  -> State.Review,
    "Flag"    -> State.Flag,
    "Archive" -> State.Archive
  )

  implicit val documentActionFormat = new EnumJsonFormat[Action](
    "Start"          -> Action.Start,
    "Submit"         -> Action.Submit,
    "Unassign"       -> Action.Unassign,
    "Resolve"        -> Action.Resolve,
    "Flag"           -> Action.Flag,
    "Archive"        -> Action.Archive,
    "PostEvidence"   -> Action.PostEvidence,
    "CreateDocument" -> Action.CreateDocument,
    "ReadDocument"   -> Action.ReadDocument
  )

  implicit val documentHistoryFormat: RootJsonFormat[DocumentHistory] = jsonFormat6(DocumentHistory.apply)

}
