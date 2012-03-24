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

  def valid(url: String) =
    if (url matches """^[^\s?&]+[^?&]*$""") url.successNel
    else "invalid url".failNel

  def available(url: String) =
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

    val url = multipartParam("url") flatMap valid flatMap available
    val password = multipartParam("password") map hash(now)
    val question = multipartParam("question")
    val answer = multipartParam("answer") map hash(now)
    val choice = multipartParam("choice") flatMap {
      case c @ ("password" | "question") => c.successNel
      case _ => "invalid choice".failNel
    }

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val file = (filepart |@| url |@| password |@| question |@| answer |@| choice) { (fp, u, p, q, a, c) =>
      import libs.Files.copyFile
      import Play.current

      val FilePart(_, name, _, ref) = fp
      val path = "files/" + u + "/" + name
      val dest = Play.getFile(path)

      copyFile(ref.file, dest)
      ref.clean

      c match {
        case "password" => new File(u, name, path, now.timestamp, to.timestamp, Some(p), None, None)
        case "question" => new File(u, name, path, now.timestamp, to.timestamp, None, Some(q), Some(a))
      }

      //new File(u, name, path, now.timestamp, to.timestamp, Some(p), Some(q), Some(a))
    }

    file.fold(failure, success)
  }

  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    Logger.debug("checkUrl request body[" + request.body + "]")

    def failure(l: NonEmptyList[String]) = l.list mkString ", "

    def success(f: String) = "available"

    val file = urlParam("url") flatMap available

    val msg = file.fold(failure, success)

    Ok(toJson(JsObject(Seq("available" -> JsBoolean(file.isSuccess), "msg" -> JsString(msg)))))
  }
}
