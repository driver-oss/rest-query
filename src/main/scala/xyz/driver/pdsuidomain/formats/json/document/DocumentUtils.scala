package xyz.driver.pdsuidomain.formats.json.document

import xyz.driver.pdsuidomain.entities.Document.Status

object DocumentUtils {

  val statusFromString: PartialFunction[String, Status] = {
    case "New"       => Status.New
    case "Organized" => Status.Organized
    case "Extracted" => Status.Extracted
    case "Done"      => Status.Done
    case "Flagged"   => Status.Flagged
    case "Archived"  => Status.Archived
  }

  def statusToString(x: Status): String = x match {
    case Status.New       => "New"
    case Status.Organized => "Organized"
    case Status.Extracted => "Extracted"
    case Status.Done      => "Done"
    case Status.Flagged   => "Flagged"
    case Status.Archived  => "Archived"
  }
}
