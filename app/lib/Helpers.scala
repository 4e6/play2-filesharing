package lib

import scalaz._
import Scalaz._

import play.api.mvc._

import akka.util.Duration
import akka.util.duration._
import java.sql.Timestamp

object Helpers {
  class NumericPimp[A: Numeric](val n: A) {
    import Numeric._
    private[this] lazy val num = implicitly[Numeric[A]]
    private[this] lazy val kibi = num.fromInt(1024)

    /* Returns bytes */
    def kB = num.times(n, kibi)
    def MB = num.times(kB, kibi)
    def GB = num.times(MB, kibi)
  }

  implicit def numPimp[A: Numeric](time: A) = new NumericPimp(time)

  implicit def durationToTimestamp(d: Duration) = new Timestamp(d.toMillis)

  val bytePrefixes = Seq("B", "kB", "MB", "GB")

  def timeNow: Duration = System.currentTimeMillis.millis

  def hash(salt: Long)(password: String): Array[Byte] = {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt.toString.getBytes)
    digest.digest(password.getBytes("UTF-8"))
  }

  def verifyData(file: models.File, key: String, data: String) = {
    file.getSecret(key) getOrElse Array.empty sameElements hash(file.creationTime.getTime)(data)
  }

  def multipartParam(key: String)(implicit request: Request[MultipartFormData[_]]) =
    request.body.dataParts.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel

  def urlParam(key: String)(implicit request: Request[Map[String, Seq[String]]]) =
    request.body.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel

  def getSomeFile(url: String) = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Storage.files lookup url)
  }
}
