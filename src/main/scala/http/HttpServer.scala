package http;

import cats.effect.IO
import com.comcast.ip4s.*
import domain.*
import domain.UrlsRef.{Backends, HealthChecks}
import services.RoundRobin.{BackendsRoundRobin, HealthChecksRoundRobin}
import services.{
  AddRequestPathToBackendUrl,
  HealthCheckBackends,
  LoadBalancer,
  ParseUri,
  SendAndExpect,
  UpdateBackendsAndGet
}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

object HttpServer {

  def start(
      backends: Backends,
      healthChecks: HealthChecks,
      port: Port,
      host: Host,
      healthCheckInterval: HealthCheckInterval,
      parseUri: ParseUri,
      updateBackendsAndGet: UpdateBackendsAndGet,
      backendsRoundRobin: BackendsRoundRobin,
      healthChecksRoundRobin: HealthChecksRoundRobin
  ): IO[Unit] =
    (
      for {
        client <- EmberClientBuilder.default[IO].build
        httpClient = HttpClient.of(client)
        httpApp = Logger.httpApp(logHeaders = false, logBody = true)(
          LoadBalancer
            .from(
              backends,
              SendAndExpect.toBackend(httpClient, _),
              parseUri,
              AddRequestPathToBackendUrl.Impl,
              backendsRoundRobin
            )
            .orNotFound
        )
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(host)
          .withPort(port)
          .withHttpApp(httpApp)
          .build
        _ <- HealthCheckBackends
          .periodically(
            healthChecks,
            backends,
            parseUri,
            updateBackendsAndGet,
            healthChecksRoundRobin,
            SendAndExpect.toHealthCheck(httpClient),
            healthCheckInterval
          )
          .toResource
      } yield ()
    ).useForever
}
