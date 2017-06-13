package xyz.driver.common.auth

class AnonymousRequestContext(val requestId: RequestId) {

  override def equals(that: Any): Boolean = {
    that.getClass == classOf[AnonymousRequestContext] &&
      that.asInstanceOf[AnonymousRequestContext].requestId == requestId
  }

  override def hashCode(): Int = requestId.hashCode()

}
