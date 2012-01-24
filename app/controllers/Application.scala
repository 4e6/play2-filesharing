package controllers

import play.api._
import play.api.mvc._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._

object Application extends Controller {
  lazy val engine = {
    val e = new TemplateEngine
    e.boot
    e
  }

  def index = Action {
    Ok(HtmlFormat raw engine.layout("views/index.jade"))
  }

  def uploadFile = Action(parse.temporaryFile) { request =>
    val f = request.body.file.getAbsolutePath
    Ok("File saved as " + f)
  }
}
