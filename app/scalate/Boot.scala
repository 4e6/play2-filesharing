package scalate

import play.api._
import Play.current
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import org.fusesource.scalate.scaml.ScamlOptions
import java.io.File

class Boot(engine: TemplateEngine) {
  lazy val path = Play.application.path

  def run() {
    engine.classLoader = Play.classloader
    engine.workingDirectory = new File(path, "target/scala-2.9.1")
    engine.sourceDirectories = new File(path, "app") :: Nil
    engine.layoutStrategy = new DefaultLayoutStrategy(engine)
    ScamlOptions.format = ScamlOptions.Format.html5

    Logger.info("Scalate configured")
  }
}
