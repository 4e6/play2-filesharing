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

  implicit def numPimp[A: Numeric](time: A) = new NumericPimp(time)

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

    (days, hours, minutes) match {
      case (0, 0, 0) => "in a minute"
      case (0, 0, _) => "%d minutes".format(minutes)
      case (0, _, _) => "%d hours %d minutes".format(hours, minutes)
      case _ => "%d days % hours %d minutes".format(days, hours, minutes)
    }
  }
}
