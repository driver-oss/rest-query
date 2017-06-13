package xyz.driver.pdsuicommon.utils

import java.text.SimpleDateFormat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonSerializer {

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JavaTimeModule)
  mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

  def serialize(value: Any): String = {
    mapper.writeValueAsString(value)
  }

  def deserialize[T](value: String)(implicit m: Manifest[T]): T = {
    mapper.readValue(value)
  }
}
