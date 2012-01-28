package controllers

import scalaz.{ Logger => SLogger, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import play.api.mvc.MultipartFormData._

import org.squeryl.PrimitiveTypeMode._
import scalax.io.Resource
import java.sql.Timestamp

import Helpers._
import models.File
import models.Files._

trait Upload {
  self: Controller =>

  def uploadFile = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("Start file uploading...")
    Logger.debug("body[" + request.body + "]")

    lazy val now = System.currentTimeMillis
    lazy val to = now + 60 * 60 * 1000

    val password = getParam("password") map { hash(now) }
    //val password: Validation[NonEmptyList[String], Array[Byte]] = "password".fail.liftFailNel

    val url = getParam("url")
    //val url: Validation[NonEmptyList[String], String] = "url".fail.liftFailNel

    val filepart = request.body.files.headOption.toSuccess("file").liftFailNel

    val file = (filepart |@| password |@| url) { (fp, p, u) =>
      val FilePart(_, name, _, ref) = fp
      val ba = Resource.fromFile(ref.file).byteArray
      new File(u, name, ba, new Timestamp(now), new Timestamp(to), Some(p), None, None)
    }

    file.fold(failure, success)
  }

  def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

  def success(f: File) = {
    transaction(files insert f)
    Ok("Success(" + f.name + ")")
  }
}

