package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.core.json._
import xyz.driver.pdsuidomain.entities._

object recordhistory {
  import DefaultJsonProtocol._
  import MedicalRecordHistory._
  import common._

  implicit val recordStateFormat = new EnumJsonFormat[State](
    "New"      -> State.New,
    "Clean"    -> State.Clean,
    "Organize" -> State.Organize,
    "Review"   -> State.Review,
    "Done"     -> State.Done,
    "Flag"     -> State.Flag,
    "Archive"  -> State.Archive
  )

  implicit val recordActionFormat = new EnumJsonFormat[Action](
    "Start"           -> Action.Start,
    "Submit"          -> Action.Submit,
    "Unassign"        -> Action.Unassign,
    "Resolve"         -> Action.Resolve,
    "Flag"            -> Action.Flag,
    "Archive"         -> Action.Archive,
    "SaveDuplicate"   -> Action.SaveDuplicate,
    "SaveReorder"     -> Action.SaveReorder,
    "SaveRotation"    -> Action.SaveRotation,
    "DeleteDuplicate" -> Action.DeleteDuplicate,
    "DeleteReorder"   -> Action.DeleteReorder,
    "DeleteRotation"  -> Action.DeleteRotation,
    "CreateDocument"  -> Action.CreateDocument,
    "DeleteDocument"  -> Action.DeleteDocument,
    "CreateRecord"    -> Action.CreateRecord,
    "ReadRecord"      -> Action.ReadRecord
  )

  implicit val recordHistoryFormat: RootJsonFormat[MedicalRecordHistory] = jsonFormat6(MedicalRecordHistory.apply)

}
