import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName = "Web Clipboard"
  val appVersion = "0.1"
  val appDependencies = Nil

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
}
