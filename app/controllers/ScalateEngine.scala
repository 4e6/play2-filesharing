package controllers

import play.api.mvc.Controller
import play.api.templates.{ Html, HtmlFormat }
import org.fusesource.scalate.TemplateEngine

trait ScalateEngine {
  self: Controller =>

  lazy val engine = {
    val e = new TemplateEngine
    e.boot
    e
  }

  def render(template: String, attributes: Map[String, Any] = Map.empty): Html =
    HtmlFormat raw engine.layout(template, attributes)

  def render(template: String, attributes: (String, Any)*): Html =
    render(template, attributes.toMap)
}
