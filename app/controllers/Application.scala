package controllers

import play.api._
import play.api.mvc._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._
import org.squeryl.PrimitiveTypeMode._
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
    val rawFile = request.body.files
    //transaction {
    //files.insert(new File(f))
    //}
    Ok("File saved as ")
  }
}
