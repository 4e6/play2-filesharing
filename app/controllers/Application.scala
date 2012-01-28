package controllers

import play.api._
import play.api.mvc._
import play.api.templates.HtmlFormat

import org.fusesource.scalate._

object Application extends Controller with ScalateEngine
    with Upload with Download {
  def index = Action {
    Ok(HtmlFormat raw engine.layout("views/index.jade"))
  }
}
