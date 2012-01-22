import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName = "Web Clipboard"

  val appVersion = "0.1"

  val appDependencies = Seq(
    "org.fusesource.scalate" % "scalate-core" % "1.5.3")

  val appResolvers = Seq(
    DefaultMavenRepository,
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.file("Local Play20 Repository", file("/opt/git-repos/play20/repository/local"))(Resolver.ivyStylePatterns))

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(
      resolvers ++= appResolvers)
}
