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

  def uploadFile = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("Start file uploading...")
    Logger.debug("body[" + request.body + "]")

    lazy val now = System.currentTimeMillis
    lazy val to = now + 1.day

    val password = multipartParam("password") map { hash(now) }
    //val password: Validation[NonEmptyList[String], Array[Byte]] = "password".fail.liftFailNel

    val url = multipartParam("url")
    //val url: Validation[NonEmptyList[String], String] = "url".fail.liftFailNel

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val file = (filepart |@| password |@| url) { (fp, p, u) =>
      val FilePart(_, name, _, ref) = fp
      val ba = scalax.io.Resource.fromFile(ref.file).byteArray
      new File(u, name, ba, now.timestamp, to.timestamp, Some(p), None, None)
    }

    file.fold(failure, success)
  }

  def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

  def success(f: File) = {
    import org.squeryl.PrimitiveTypeMode._

    transaction(files insert f)
    Ok("Success(" + f.name + ")")
  }

  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    Logger.debug("checkUrl request body[" + request.body + "]")

    val file = urlParam("url") >>= getSomeFile
    val msg = file.fold(_ => "reserved", "available")
    Ok(toJson(JsObject(Seq("msg" -> JsString(msg)))))
  }
}
