package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import MultipartFormData.FilePart

import Helpers._
import models._

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

    def success(t: (File, Task)) = {
      import org.squeryl.PrimitiveTypeMode._
      val (file, task) = t
      transaction {
        Storage.files insert file
        Storage.schedule insert task
      }
      Ok("Success(" + file.name + ")")
    }

    lazy val now = System.currentTimeMillis
    lazy val to = now + 1.mins

    val url = multipartParam("url") flatMap valid flatMap available
    val password = multipartParam("password") map hash(now)
    val question = multipartParam("question")
    val answer = multipartParam("answer") map hash(now)
    val choice = multipartParam("choice") flatMap {
      case c @ ("password" | "question") => c.successNel
      case _ => "invalid choice".failNel
    }

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val result = (filepart |@| url |@| password |@| question |@| answer |@| choice) { (fp, u, p, q, a, c) =>

      val FilePart(_, name, _, ref) = fp
      val size = ref.file.length
      val dest = Storage.root / u / name

      scalax.file.Path(ref.file) copyTo dest
      ref.clean

      val file = c match {
        case "password" => new File(u, name, size, now.timestamp, to.timestamp, Some(p), None, None)
        case "question" => new File(u, name, size, now.timestamp, to.timestamp, None, Some(q), Some(a))
      }

      val task = new Task(u, to.timestamp)
      (file, task)
    }

    result.fold(failure, success)
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
