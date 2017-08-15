package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.StudyDesign

class StudyDesignFormatSuite extends FlatSpec with Matchers {
  import studydesign._

  "Json format for StudyDesign" should "read and write correct JSON" in {
    val studyDesign = StudyDesign(
      id = LongId(10),
      name = "study design name"
    )
    val writtenJson = studyDesignFormat.write(studyDesign)

    writtenJson should be("""{"id":10,"name":"study design name"}""".parseJson)

    val parsedStudyDesign = studyDesignFormat.read(writtenJson)
    parsedStudyDesign should be(studyDesign)
  }

}
