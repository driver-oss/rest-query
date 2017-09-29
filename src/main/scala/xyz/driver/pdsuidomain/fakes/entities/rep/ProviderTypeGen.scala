package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.core.generators.nextString
import xyz.driver.pdsuidomain.entities.ProviderType
import xyz.driver.pdsuidomain.fakes.entities.common.nextLongId

object ProviderTypeGen {
  def nextProviderType(): ProviderType = {
    ProviderType(
      id = nextLongId[ProviderType],
      name = nextString()
    )
  }
}
