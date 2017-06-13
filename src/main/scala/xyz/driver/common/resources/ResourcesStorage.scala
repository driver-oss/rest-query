package xyz.driver.common.resources

import scala.io.{Codec, Source}

trait ResourcesStorage {

  /**
    * @param resourcePath Don't forget / at start
    */
  def getFirstLine(resourcePath: String): String

}

object RealResourcesStorage extends ResourcesStorage {

  def getFirstLine(resourcePath: String): String = {
    val resourceUrl = getClass.getResource(resourcePath)
    Option(resourceUrl) match {
      case Some(url) =>
        val source = Source.fromURL(resourceUrl)(Codec.UTF8)
        try {
          val lines = source.getLines()
          if (lines.isEmpty) throw new RuntimeException(s"'$resourcePath' is empty")
          else lines.next()
        } finally {
          source.close()
        }
      case None =>
        throw new RuntimeException(s"Can not find the '$resourcePath'!")
    }
  }

}

object FakeResourcesStorage extends ResourcesStorage {

  def getFirstLine(resourcePath: String): String = ""

}
