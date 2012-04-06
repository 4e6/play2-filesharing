package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import MultipartFormData.FilePart
import akka.util.duration._

import models._
import lib.Helpers._

trait Upload {
  self: Controller =>

  def apiUpload = Action(parse.multipartFormData) { implicit request =>
    def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

    def success(record: Record) = {
      import org.squeryl.PrimitiveTypeMode._
      transaction(Storage.records insert record)
      Ok("Success(" + record.name + ")")
    }

    lazy val now = timeNow

    val file = Record.File.apply
    val url = Record.URL(file)
    val password = Record.Password(now)
    val question = Record.Question.apply
    val answer = Record.Answer(now)

    val result = Record(file, url, password, question, answer, now)

    result.fold(failure, success)
  }

  def valid(url: String) =
    if (url matches """^[^\s?&]+[^?&]*$""") url.successNel
    else "invalid url".failNel

  def available(url: String) =
    getSomeFile(url).cata(_ => "reserved".failNel, url.successNel)

  def uploadFile = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("Start file uploading...")
    Logger.debug("body[" + request.body + "]")

    def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

    def success(t: (Record, Task)) = {
      import org.squeryl.PrimitiveTypeMode._
      val (file, task) = t
      transaction {
        Storage.records insert file
        Storage.schedule insert task
      }
      Ok("Success(" + file.name + ")")
    }

    lazy val now = timeNow
    lazy val to = now + 1.minute

    val url = getParam("url") flatMap valid flatMap available
    val password = getParam("password") map hash(now.toMillis)
    val question = getParam("question")
    val answer = getParam("answer") map hash(now.toMillis)
    val choice = getParam("choice") flatMap {
      case c @ ("password" | "question") => c.successNel
      case _ => "invalid choice".failNel
    }

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val result = (filepart |@| url |@| password |@| question |@| answer |@| choice) { (fp, u, p, q, a, c) =>
      val FilePart(_, name, _, ref) = fp
      val size = ref.file.length
      val dest = Storage.root / u / name

      /* Workaround for scala-io 'moveTo bug
       * https://github.com/jesseeichar/scala-io/issues/54*/
      scalax.file.Path(ref.file) copyTo dest
      ref.clean

      val file = c match {
        case "password" => new Record(u, name, size, now, to, Some(p), None, None)
        case "question" => new Record(u, name, size, now, to, None, Some(q), Some(a))
      }

      val task = new Task(u, to)
      (file, task)
    }

    result.fold(failure, success)
  }

  /** Check url availability*/
  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    Logger.debug("checkUrl request body[" + request.body + "]")

    def failure(l: NonEmptyList[String]) = l.list mkString ", "

    def success(f: String) = "available"

    val file = getParam("url") flatMap available

    val msg = file.fold(failure, success)

    Ok(toJson(JsObject(Seq("available" -> JsBoolean(file.isSuccess), "msg" -> JsString(msg)))))
  }
}
