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

  def uploadFile = Action(parse.multipartFormData) { request =>
    Logger.debug("Start file uploading...")
    Logger.debug("body[" + request.body + "]")

    lazy val timeNow = System.currentTimeMillis
    lazy val timeToStore = timeNow + 60 * 60 * 1000

    val password = request.body.dataParts.get("password")
      .flatMap { _.headOption }
      .map { hash(timeNow.toString.getBytes) }

    val uploadedFile = request.body.files.headOption map { filepart =>
      val FilePart(_, filename, _, ref) = filepart
      (filename, ref.file)
    }

    uploadedFile.cata(f => success("", f, timeNow, timeToStore, password, None, None), failure)
  }

  def success(url: String,
              nameFile: (String, java.io.File),
              from: Long,
              to: Long,
              pass: Option[Array[Byte]],
              q: Option[String],
              a: Option[Array[Byte]]) = {
    val (name, file) = nameFile
    transaction {
      files insert new File(
        url,
        name,
        Resource.fromFile(file).byteArray,
        new Timestamp(from),
        new Timestamp(to),
        pass, q, a)
    }
    Ok("File saved as " + name)
  }

  def failure = Ok("File upload failed")
}
