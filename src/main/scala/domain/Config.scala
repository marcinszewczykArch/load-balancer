package domain

import pureconfig._
import pureconfig.generic.derivation.default._

final case class Config(
    port: Int,
    host: String,
    backends: Urls,
    healthCheckInterval: HealthCheckInterval
) derives ConfigReader

object Config {

  given urlReader: ConfigReader[Url] = ConfigReader[String].map(Url.apply)

  given urlsReader: ConfigReader[Urls] =
    ConfigReader[Vector[Url]].map(Urls.apply)

  given healthCheckReader: ConfigReader[HealthCheckInterval] =
    ConfigReader[Long].map(HealthCheckInterval.apply)
}
