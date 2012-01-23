import sbt._
import Keys._
import PlayProject._
import com.mojolly.scalate.ScalatePlugin._

object ApplicationBuild extends Build {
  val appName = "Web Clipboard"

  val appVersion = "0.1"

  val appDependencies = Seq(
    "org.fusesource.scalate" % "scalate-core" % "1.5.3",
    "org.slf4j" % "slf4j-nop" % "1.6.4" % "scalate"
  )

  val appResolvers = Seq(
    DefaultMavenRepository,
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.file("Local Play20 Repository", file("/opt/git-repos/play20/repository/local"))(Resolver.ivyStylePatterns))

  val appSettings = Seq(
    resolvers ++= appResolvers
  )

  val scalatePluginSettings = seq(scalateSettings: _*) ++ Seq(
    scalateTemplateDirectory in Compile <<= (baseDirectory) { _ / "app/views"},
    scalateOverwrite := false
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
  .settings(appSettings: _*)
  .settings(scalatePluginSettings: _*)
}
