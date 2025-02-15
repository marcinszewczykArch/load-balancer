package services

import cats.syntax.either.*
import errors.parsing.InvalidUri
import org.http4s.Uri

trait ParseUri {
  def apply(uri: String): Either[Throwable, Uri]
}

object ParseUri {

  object Impl extends ParseUri {

    /** Either returns proper Uri or InvalidUri
      */
    override def apply(uri: String): Either[Throwable, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))
  }
}
