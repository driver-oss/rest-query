package xyz.driver.pdsuidomain.formats.json

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.Hypothesis

class HypothesisFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.hypothesis._

  "Json format for Hypothesis" should "read and write correct JSON" in {
    val hypothesis = Hypothesis(
      id = UuidId("3b80b2e2-5372-4cf5-a342-6e4ebe10fafd"),
      name = "hypothesis name",
      treatmentType = "treatment type",
      description = "descr"
    )
    val writtenJson = hypothesisFormat.write(hypothesis)

    writtenJson should be("""{"id":"3b80b2e2-5372-4cf5-a342-6e4ebe10fafd","name":"hypothesis name",
        "treatmentType":"treatment type","description":"descr"}""".parseJson)

    val parsedHypothesis = hypothesisFormat.read(writtenJson)
    parsedHypothesis should be(hypothesis)
  }

}
