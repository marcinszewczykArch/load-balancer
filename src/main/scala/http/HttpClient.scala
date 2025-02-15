package http

import cats.effect.IO
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt

trait HttpClient {
  def sendAndReceive(uri: Uri, requestOpt: Option[Request[IO]]): IO[String]
}

object HttpClient {

  def of(client: Client[IO]): HttpClient = new HttpClient {
    override def sendAndReceive(
        uri: Uri,
        requestOpt: Option[Request[IO]]
    ): IO[String] =
      requestOpt match {
        case Some(request) => client.expect[String](request.withUri(uri))
        case None          => client.expect[String](uri)
      }
  }

  val Hello: HttpClient = (_, _) => IO.pure("Hello")
  val RuntimeException: HttpClient = (_, _) =>
    IO.raiseError(new RuntimeException("Server is dead"))
  val TestTimeoutFailure: HttpClient = (_, _) => IO.sleep(6.seconds).as("")
  val BackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError {
      UnexpectedStatus(
        org.http4s.Status.NotFound,
        org.http4s.Method.GET,
        Uri.unsafeFromString("localhost:8081")
      )
    }
}
