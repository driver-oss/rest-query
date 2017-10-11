package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities.ExtractedData.Meta
import xyz.driver.pdsuidomain.entities.{ExtractedData, ExtractedDataLabel}
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

class ExtractedDataFormatSuite extends FlatSpec with Matchers {
  import extracteddata._

  "Json format for ExtractedData" should "read and write correct JSON" in {
    val extractedData = ExtractedData(
      id = LongId(1),
      documentId = LongId(101),
      keywordId = Some(LongId(201)),
      evidenceText = Some("evidence text"),
      meta = None
    )
    val extractedDataLabels = List(
      ExtractedDataLabel(
        id = LongId(1),
        dataId = extractedData.id,
        labelId = None,
        categoryId = None,
        value = Some(LabelValue.Yes)
      ),
      ExtractedDataLabel(
        id = LongId(2),
        dataId = extractedData.id,
        labelId = Some(LongId(12)),
        categoryId = Some(LongId(1)),
        value = Some(LabelValue.No)
      )
    )
    val origRichExtractedData = RichExtractedData(
      extractedData = extractedData,
      labels = extractedDataLabels
    )
    val writtenJson = extractedDataFormat.write(origRichExtractedData)

    writtenJson should be(
      """{"id":1,"documentId":101,"keywordId":201,"evidence":"evidence text","meta":null,
        "labels":[{"id":null,"categoryId":null,"value":"Yes"},{"id":12,"categoryId":1,"value":"No"}]}""".parseJson)

    val createExtractedDataJson =
      """{"documentId":101,"keywordId":201,"evidence":"evidence text",
        "labels":[{"value":"Yes"},{"id":12,"categoryId":1,"value":"No"}]}""".parseJson
    val expectedCreatedExtractedData = origRichExtractedData.copy(
      extractedData = extractedData.copy(id = LongId(0)),
      labels = extractedDataLabels.map(_.copy(id = LongId(0), dataId = LongId(0)))
    )
    val parsedCreatedExtractedData = extractedDataFormat.read(createExtractedDataJson)
    parsedCreatedExtractedData should be(expectedCreatedExtractedData)

    val updateExtractedDataJson =
      """{"evidence":"new evidence text","meta":{"keyword":{"page":1,"index":2,"sortIndex":"ASC"},
         "evidence":{"pageRatio":1.0,"start":{"page":1,"index":3,"offset":2},"end":{"page":2,"index":3,"offset":10}}},
        "labels":[{"id":20,"categoryId":1,"value":"Yes"},{"id":12,"categoryId":1,"value":"No"}]}""".parseJson
    val updatedExtractedDataLabels = List(
      ExtractedDataLabel(
        id = LongId(0),
        dataId = extractedData.id,
        labelId = Some(LongId(20)),
        categoryId = Some(LongId(1)),
        value = Some(LabelValue.Yes)
      ),
      ExtractedDataLabel(
        id = LongId(0),
        dataId = extractedData.id,
        labelId = Some(LongId(12)),
        categoryId = Some(LongId(1)),
        value = Some(LabelValue.No)
      )
    )
    val expectedUpdatedExtractedData = origRichExtractedData.copy(
      extractedData = extractedData.copy(
        evidenceText = Some("new evidence text"),
        meta = Some(
          TextJson(Meta(
            keyword = Meta.Keyword(page = 1, pageRatio = None, index = 2, sortIndex = "ASC"),
            evidence = Meta.Evidence(
              pageRatio = 1.0,
              start = Meta.TextLayerPosition(page = 1, index = 3, offset = 2),
              end = Meta.TextLayerPosition(page = 2, index = 3, offset = 10)
            )
          )))
      ),
      labels = updatedExtractedDataLabels
    )
    val parsedUpdatedExtractedData = applyUpdateToExtractedData(updateExtractedDataJson, origRichExtractedData)
    parsedUpdatedExtractedData should be(expectedUpdatedExtractedData)
  }

}
