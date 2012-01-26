package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.MultipartFormData._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._
import org.squeryl.PrimitiveTypeMode._
import scalax.io.Resource
import java.sql.Timestamp

import Helpers._
import models.File
import models.Files._

object Application extends Controller
  with Download {
  lazy val engine = {
    val e = new TemplateEngine
    e.boot
    e
  }

  def index = Action {
    Ok(HtmlFormat raw engine.layout("views/index.jade"))
  }

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

    if (uploadedFile.isDefined) {
      val (filename, file) = uploadedFile.get
      val url = "http://localhost/" + filename
      transaction {
        files insert {
          new File(
            url,
            filename,
            Resource.fromFile(file).byteArray,
            new Timestamp(timeNow),
            new Timestamp(timeToStore),
            password,
            None,
            None)
        }
      }
      Ok("File saved as " + filename)
    } else
      Ok("File upload failed")
  }
}
