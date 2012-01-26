import org.fusesource.scalate.{ TemplateSource, Binding }
import org.fusesource.scalate.support.TemplatePackage

/** Standart imports for Play templates */
class ScalatePackage extends TemplatePackage {
  override def header(source: TemplateSource, bindings: List[Binding]) =
    """
    | import play.api.templates._
    | import play.api.templates.PlayMagic._
    | import models._
    | import controllers._
    | import play.api.i18n.Messages
    | import play.api.mvc._
    | import play.api.data._
    """.stripMargin
}
