package lib

import scalaz._
import Scalaz._

import play.api.mvc._
import play.api.libs.Files.TemporaryFile

import akka.util.Duration
import akka.util.duration._
import java.sql.Timestamp

object Helpers {
  class NumericPimp[A: Numeric](val n: A) {
    private[this] lazy val num = implicitly[Numeric[A]]
    private[this] lazy val kibi = num.fromInt(1024)

    /* Returns bytes */
    def K = num.times(n, kibi)
    def M = num.times(K, kibi)
    def G = num.times(M, kibi)
    def T = num.times(G, kibi)
  }

  implicit def numericPimp[A: Numeric](n: A) = new NumericPimp(n)

  implicit def longToTimestamp(l: Long) = new Timestamp(l)

  implicit def longToDuration(l: Long) = l.millis

  def timeNow = System.currentTimeMillis

  val bytePrefixes = Seq("B", "kB", "MB", "GB")

  def hash(salt: Long)(password: String): Array[Byte] = {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt.toString.getBytes)
    digest.digest(password.getBytes("UTF-8"))
  }

  def getParam[T](key: String)(implicit request: Request[T]) = {
    val body = request.body match {
      case body: MultipartFormData[_] => body.asFormUrlEncoded
      case body: AnyContent => request.queryString
      case body: Map[String, Seq[String]] => body
    }
    body.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel
  }

  def readableTime(timeMillis: Long) = {
    val minutes = timeMillis.toMinutes - timeMillis.toHours.hours.toMinutes
    val hours = timeMillis.toHours - timeMillis.toDays.days.toHours
    val days = timeMillis.toDays

    if (days == 0) "%2dh %2dm".format(hours, minutes)
    else "%dd %2dh %2dm".format(days, hours, minutes)
  }

  def readableSize(size: Long) = {
    val mask = "%.1f"
    def convert(size: Double, px: Seq[String]): String = {
      val next = size / 1024
      if (px.nonEmpty && next > 1) convert(next, px.tail)
      else mask.format(size) + " " + px.head
    }

    convert(size, bytePrefixes)
  }
}
