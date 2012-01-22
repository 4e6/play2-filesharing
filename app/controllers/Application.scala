package controllers

import play.api._
import play.api.mvc._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._

object Application extends Controller {
  lazy val engine = new TemplateEngine

  def index = Action {
    //Ok(views.html.index())
    val bindings = Map(
      "title" -> "Index",
      "content" -> "empty"
    )
    engine.boot
    Ok(HtmlFormat raw engine.layout("index.jade", bindings))
  }
}
