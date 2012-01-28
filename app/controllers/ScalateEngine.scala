package controllers

import play.api.mvc.Controller
import play.api.templates.HtmlFormat
import org.fusesource.scalate.TemplateEngine

trait ScalateEngine {
  self: Controller =>

  lazy val engine = {
    val e = new TemplateEngine
    e.boot
    e
  }

  def render(template: String, attributes: Map[String, Any] = Map()) =
    HtmlFormat raw engine.layout(template, attributes)

  def render(template: String, attributes: (String, Any)*) =
    HtmlFormat raw engine.layout(template, attributes.toMap)
}
