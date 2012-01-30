package controllers

import scalaz._
import Scalaz._

import play.api._
import play.api.mvc._
import java.security.MessageDigest

object Helpers {
  def hash(salt: Long)(password: String): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt.toString.getBytes)
    digest.digest(password.getBytes("UTF-8"))
  }

  def multipartParam(key: String)(implicit request: Request[MultipartFormData[_]]) =
    request.body.dataParts.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel

  def urlParam(key: String)(implicit request: Request[Map[String, Seq[String]]]) =
    request.body get key >>= (_.headOption)

  def getSomeFile(url: String) = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Files.files lookup url)
  }
}
