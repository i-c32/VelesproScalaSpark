package VelESPro

import com.typesafe.config.Config

object ConfigCheck {

  implicit class RichConfig(val config: Config) extends AnyVal {
    def getOBoolean(path: String): Option[Boolean] =
      Option.when(config.hasPath(path))(config.getBoolean(path))

  }
}
