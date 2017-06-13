package xyz.driver.common.domain

import java.nio.charset.Charset

import org.mindrot.jbcrypt.BCrypt

case class PasswordHash(value: Array[Byte]) {

  lazy val hashString: String = new String(value, Charset.forName("UTF-8"))

  override def toString: String = {
    s"${this.getClass.getSimpleName}($hashString)"
  }

  override def equals(that: Any): Boolean = {
    that match {
      case thatHash: PasswordHash => java.util.Arrays.equals(this.value, thatHash.value)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    42 + java.util.Arrays.hashCode(this.value)
  }

  def is(password: String): Boolean = {
    BCrypt.checkpw(password, hashString)
  }

}

object PasswordHash {

  def apply(password: String): PasswordHash = {
    new PasswordHash(getHash(password))
  }

  private def getHash(str: String): Array[Byte] = {
    BCrypt.hashpw(str, BCrypt.gensalt()).getBytes(Charset.forName("UTF-8"))
  }

}
