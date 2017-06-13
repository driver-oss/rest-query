package xyz.driver.pdsuicommon.db

import xyz.driver.pdsuicommon.domain.Id

class EntityNotFoundException private (id: String, tableName: String)
    extends RuntimeException(s"Entity with id $id is not found in $tableName table") {

  def this(id: Id[_], tableName: String) = this(id.toString, tableName)
  def this(id: Long, tableName: String) = this(id.toString, tableName)
}
