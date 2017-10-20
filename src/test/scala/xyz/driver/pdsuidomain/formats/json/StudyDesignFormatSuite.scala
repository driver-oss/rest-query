package xyz.driver.pdsuidomain.formats.json

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuidomain.entities.StudyDesign

class StudyDesignFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.studydesign._

  "Json format for StudyDesign" should "read and write correct JSON" in {
    val studyDesign = StudyDesign.Randomized
    val writtenJson = studyDesignFormat.write(studyDesign)

    writtenJson should be("""{"id":1,"name":"Randomized"}""".parseJson)

    val parsedStudyDesign = studyDesignFormat.read(writtenJson)
    parsedStudyDesign should be(studyDesign)
  }

}
