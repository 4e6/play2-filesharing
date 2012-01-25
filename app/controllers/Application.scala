package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.MultipartFormData._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._
import org.squeryl.PrimitiveTypeMode._
import scalax.io.Resource
import java.sql.Timestamp

import models.File
import models.Files._

object Application extends Controller {
  lazy val engine = {
    val e = new TemplateEngine
    e.boot
    e
  }

  def index = Action {
    Ok(HtmlFormat raw engine.layout("views/index.jade"))
  }

  def uploadFile = Action(parse.multipartFormData) { request =>
    Logger.info("Start file uploading...")
    Logger.info("body[" + request.body + "]")

    val password = request.body.dataParts.get("password") flatMap { _.headOption }

    val uploadedFile = request.body.files.headOption map { filepart =>
      val FilePart(_, filename, _, ref) = filepart
      (filename, ref.file)
    }

    if (uploadedFile.isDefined) {
      val (filename, file) = uploadedFile.get
      val url = "http://localhost/" + filename
      transaction {
        files insert {
          new File(url,
                   filename,
                   Resource.fromFile(file).byteArray,
                   new Timestamp(System.currentTimeMillis),
                   new Timestamp(System.currentTimeMillis + 60*60*1000),
                   password,
                   None,
                   None)
        }
      }
      Ok("File saved as " + uploadedFile.get._1)
    } else
      Ok("File upload failed")
  }
}
