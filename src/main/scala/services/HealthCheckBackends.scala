package services

import cats.effect.IO
import domain.{HealthCheckInterval, Urls}
import domain.UrlsRef.{Backends, HealthChecks}
import http.ServerHealthStatus
import services.RoundRobin.HealthChecksRoundRobin

import scala.concurrent.duration.DurationLong

object HealthCheckBackends {

  def periodically(
      healthChecks: HealthChecks,
      backends: Backends,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      healthChecksRoundRobin: HealthChecksRoundRobin,
      sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
      healthCheckInterval: HealthCheckInterval
  ): IO[Unit] =
    checkHealthAndUpdateBackends(
      healthChecks,
      backends,
      parseUri,
      updateBackendsAndGet,
      healthChecksRoundRobin,
      sendAndExpectStatus
    ).flatMap(_ => IO.sleep(healthCheckInterval.value.seconds))
      .foreverM // todo: use fs2 stream instead of sleep

  private[services] def checkHealthAndUpdateBackends(
      healthChecks: HealthChecks,
      backends: Backends,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      healthChecksRoundRobin: HealthChecksRoundRobin,
      sendAndExpectStatus: SendAndExpect[ServerHealthStatus]
  ): IO[Urls] =
    for {
      currentUrl <- healthChecksRoundRobin(healthChecks)
      uri <- IO.fromEither(parseUri(currentUrl.value))
      status <- sendAndExpectStatus(uri)
      updated <- updateBackendsAndGet(backends, currentUrl, status)
    } yield updated
}
