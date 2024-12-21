package domain

import pureconfig.ConfigReader

final case class Url(value: String) extends AnyVal:
  override def toString: String = value
