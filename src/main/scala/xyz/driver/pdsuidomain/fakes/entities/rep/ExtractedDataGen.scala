package xyz.driver.pdsuidomain.fakes.entities.rep


import xyz.driver.core.generators._
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.entities.ExtractedData.Meta
import xyz.driver.pdsuidomain.fakes.entities.common._
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData


object ExtractedDataGen {
  private val maxItemsInCollectionNumber = 50

  private val maxPageNumber  = 100
  private val maxIndexNumber = 100
  private val maxOffsetNumber = 10

  implicit private class TextLayerPositionOrdering(textLayerPosition: ExtractedData.Meta.TextLayerPosition)
    extends Ordered[ExtractedData.Meta.TextLayerPosition] {
    override def compare(that: Meta.TextLayerPosition): Int = {
      if (this.textLayerPosition.page < that.page) -1
      else if (this.textLayerPosition.page > that.page) 1
      else if (this.textLayerPosition.index < that.index) -1
      else if (this.textLayerPosition.index > that.index) 1
      else if (this.textLayerPosition.offset < that.offset) -1
      else if (this.textLayerPosition.offset > that.offset) 1
      else 0
    }
  }

  def nextExtractedDataMetaKeyword(): Meta.Keyword = {
    ExtractedData.Meta.Keyword(
      page = nextInt(maxPageNumber, minValue = 0),
      pageRatio = nextOption(nextDouble()),
      index = nextInt(maxIndexNumber, minValue = 0),
      sortIndex = nextString()
    )
  }

  def nextExtractedDataMetaTextLayerPosition(): Meta.TextLayerPosition = {
    ExtractedData.Meta.TextLayerPosition(
      page = nextInt(maxPageNumber, minValue = 0),
      index = nextInt(maxIndexNumber, minValue = 0),
      offset = nextInt(maxOffsetNumber, minValue = 0)
    )
  }

  def nextExtractedDataMetaEvidence(): Meta.Evidence = {
    val layersPosition =
      Common.genBoundedRange[ExtractedData.Meta.TextLayerPosition](
        nextExtractedDataMetaTextLayerPosition(),
        nextExtractedDataMetaTextLayerPosition()
      )

    ExtractedData.Meta.Evidence(
      pageRatio = nextDouble(),
      start = layersPosition._1,
      end   = layersPosition._2
    )
  }

  def nextExtractedDataMeta(): Meta = {
    ExtractedData.Meta(
      nextExtractedDataMetaKeyword(),
      nextExtractedDataMetaEvidence()
    )
  }

  def nextExtractedDataMetaJson(): TextJson[Meta] = {
    TextJson(
      ExtractedData.Meta(
        nextExtractedDataMetaKeyword(),
        nextExtractedDataMetaEvidence()
      )
    )
  }


  def nextExtractedData(documentId: LongId[Document]): ExtractedData = {
    ExtractedData(
      id = nextLongId[ExtractedData],
      documentId = documentId,
      keywordId = nextOption(nextLongId[xyz.driver.pdsuidomain.entities.Keyword]),
      evidenceText = nextOption(nextString()),
      meta = nextOption(nextExtractedDataMetaJson())
    )
  }


  def nextExtractedDataLabel(): ExtractedDataLabel = {
    ExtractedDataLabel(
      id = nextLongId[ExtractedDataLabel],
      dataId = nextLongId[ExtractedData],
      labelId = nextOption(nextLongId[Label]),
      categoryId = nextOption(nextLongId[Category]),
      value = nextOption(Common.nextFuzzyValue())
    )
  }

  def nextRichExtractedData(documentId: LongId[Document] = nextLongId): RichExtractedData = {
    RichExtractedData(
      extractedData = nextExtractedData(documentId),
      labels = List.fill(
        nextInt(maxItemsInCollectionNumber, minValue = 0)
      )(nextExtractedDataLabel())
    )
  }
}
