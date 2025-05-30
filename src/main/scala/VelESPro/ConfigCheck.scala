package VelESPro

import com.typesafe.config.Config

object ConfigCheck {

  implicit class RichConfig(val config: Config) extends AnyVal {
    def getOString(path: String): Option[String] = if (config.hasPath(path)) {
      Some(config.getString(path))
    } else {
      None
    }

    def getOBoolean(path: String): Option[Boolean] = if (config.hasPath(path)) {
      Some(config.getBoolean(path))
    } else {
      None
    }

    def getOInt(path: String): Option[Int] = if (config.hasPath(path)) {
      Some(config.getInt(path))
    } else {
      None
    }

  }
}
