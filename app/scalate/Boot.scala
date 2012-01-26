package scalate

import play.api._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.File

class Boot(engine: TemplateEngine) {
  lazy val path = Play.current.path

  def run() {
    engine.workingDirectory = new File(path, "/target/scala-2.9.1")
    engine.sourceDirectories = Seq(new File(path, "/app"))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine)
    Logger("play").info("Scalate configured")
  }
}
