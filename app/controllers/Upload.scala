package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import MultipartFormData.FilePart

import Helpers._
import models.File
import models.Files._

trait Upload {
  self: Controller =>

  def isValid(url: String) =
    if (url matches """^[^\s?&]+[^?&]*$""") url.successNel
    else "invalid url".failNel

  def isAvailable(url: String) =
    getSomeFile(url).cata(_ => "reserved".failNel, url.successNel)

  def uploadFile = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("Start file uploading...")
    Logger.debug("body[" + request.body + "]")

    def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

    def success(f: File) = {
      import org.squeryl.PrimitiveTypeMode._
      transaction(files insert f)
      Ok("Success(" + f.name + ")")
    }

    lazy val now = System.currentTimeMillis
    lazy val to = now + 1.day

    val password = multipartParam("password") map hash(now)

    val url = multipartParam("url") flatMap isValid flatMap isAvailable

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val file = (filepart |@| password |@| url) { (fp, p, u) =>
      import libs.Files.copyFile
      import Play.current

      val FilePart(_, name, _, ref) = fp
      val path = "files/" + u + "/" + name
      val dest = Play.getFile(path)

      copyFile(ref.file, dest)
      ref.clean

      new File(u, name, path, now.timestamp, to.timestamp, Some(p), None, None)
    }

    file.fold(failure, success)
  }

  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    Logger.debug("checkUrl request body[" + request.body + "]")

    def failure(l: NonEmptyList[String]) = l.list mkString ", "

    def success(f: String) = "available"

    val file = urlParam("url") flatMap isAvailable

    val msg = file.fold(failure, success)

    Ok(toJson(JsObject(Seq("available" -> JsBoolean(file.isSuccess), "msg" -> JsString(msg)))))
  }
}
