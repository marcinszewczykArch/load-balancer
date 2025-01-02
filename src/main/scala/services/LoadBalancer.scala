package services

import domain.UrlsRef.Backends
import org.http4s.Uri.Path.Segment
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}
import cats.effect.IO
import services.RoundRobin.BackendsRoundRobin

object LoadBalancer {
def from(
          backends: Backends,
          sendAndExpectResponse: Request[IO] => SendAndExpect[String],
          parseUri: ParseUri,
          addRequestPathToBackendUrl: AddRequestPathToBackendUrl,
          backendsRoundRobin: BackendsRoundRobin,
        ): HttpRoutes[IO] = {
  val dsl = new Http4sDsl[IO] {}
  import dsl._
  HttpRoutes.of[IO] { request =>
    backendsRoundRobin(backends).flatMap {
      _.fold(Ok("All backends are inactive")) { backendUrl =>
        val url = addRequestPathToBackendUrl(backendUrl.value, request)
        for {
          uri      <- IO.fromEither(parseUri(url))
          response <- sendAndExpectResponse(request)(uri)
          result   <- Ok(response)
        } yield result
      }
    }
  }
}
}