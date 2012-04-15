package views

import org.fusesource.scalate.{ TemplateSource, Binding }
import org.fusesource.scalate.support.TemplatePackage

/** Standart imports for Play templates */
class ScalatePackage extends TemplatePackage {
  override def header(source: TemplateSource, bindings: List[Binding]) =
    """
    | import controllers._
    | import models._
    """.stripMargin
}

