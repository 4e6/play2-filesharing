import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName = "Web Clipboard"

  val appVersion = "0.1"

  val appDependencies = Seq(
    "org.fusesource.scalate" % "scalate-core" % "1.5.3",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1"
  )

  val appResolvers = Seq(
    DefaultMavenRepository,
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.file("Local Play20 Repository", file("/opt/git-repos/play20/repository/local"))(Resolver.ivyStylePatterns))

  val appSettings = Seq(
    resolvers ++= appResolvers,
    javaOptions in run += "-Xmx2048M"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
  .settings(appSettings: _*)
}
