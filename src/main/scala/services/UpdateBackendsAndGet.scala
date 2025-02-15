package services

import cats.effect.IO
import domain.{Url, Urls}
import domain.UrlsRef.Backends
import http.ServerHealthStatus

trait UpdateBackendsAndGet {
  def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Urls]
}

object UpdateBackendsAndGet {

  object Impl extends UpdateBackendsAndGet {
    override def apply(
        backends: Backends,
        url: Url,
        status: ServerHealthStatus
    ): IO[Urls] =
      backends.urls.updateAndGet { urls =>
        status match {
          case ServerHealthStatus.Alive => urls.add(url)
          case ServerHealthStatus.Dead  => urls.remove(url)
        }
      }
  }
}
