package scalate

import play.api._
import org.fusesource.scalate.TemplateEngine
import java.io.File

class Boot(engine: TemplateEngine) {
  lazy val path = Play.current.path
  
  def run() {
    engine.workingDirectory = new File(path, "/target/scalate")
    engine.sourceDirectories = Seq(new File(path, "/app/views"))
    Logger("play").info("Scalate configured")
  }
}
