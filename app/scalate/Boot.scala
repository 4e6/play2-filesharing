package scalate

import play.api._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.File

class Boot(engine: TemplateEngine) {
  lazy val path = Play.current.path
  
  def run() {
    engine.workingDirectory = new File(path, "/target/scalate")
    engine.sourceDirectories = Seq(new File(path, "/app/views"))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine)
    Logger("play").info("Scalate configured")
  }
}
