package services

import cats.Id
import cats.effect.IO
import cats.syntax.option.*
import domain.{Url, Urls, UrlsRef}

import scala.util.Try

trait RoundRobin[F[_]] {
  def apply(ref: UrlsRef): IO[F[Url]]
}

object RoundRobin {

  type BackendsRoundRobin     = RoundRobin[Option]
  type HealthChecksRoundRobin = RoundRobin[Id]

  def forBackends: BackendsRoundRobin = new BackendsRoundRobin:
    override def apply(ref: UrlsRef): IO[Option[Url]] =
      ref.urls
        .getAndUpdate(next)
        .map(_.currentOpt)

  def forHealthChecks: HealthChecksRoundRobin = new HealthChecksRoundRobin:
    override def apply(ref: UrlsRef): IO[Id[Url]] =
      ref.urls
        .getAndUpdate(next)
        .map(_.currentUnsafe)

  private def next(urls: Urls): Urls =
    Try(Urls(urls.values.tail :+ urls.values.head))
      .getOrElse(Urls.empty)

  val TestId: RoundRobin[Id]            = _ => IO.pure(Url("localhost:8081"))
  val LocalHost8081: RoundRobin[Option] = _ => IO.pure(Some(Url("localhost:8081")))
}