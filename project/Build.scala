import sbt._
import Keys._
import PlayProject._
import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._

object ApplicationBuild extends Build {
  val appName = "Web Clipboard"

  val appVersion = "0.1"

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "org.fusesource.scalate" % "scalate-core" % "1.5.3",
    "org.squeryl" %% "squeryl" % "0.9.5-RC1",
    "org.scalaz" %% "scalaz-core" % "6.0.4"
  )

  val appResolvers = Seq()

  val appSettings = Seq(
    resolvers ++= appResolvers,
    watchSources <++= baseDirectory map { path => ((path / "app" / "views") ** "*.jade").get },
    scalacOptions ++= Seq("-Xlint", "-deprecation"),
    javaOptions in run += "-Xmx2G -XX:MaxPermSize=512m",
    ensimeConfig := sexp(
      key(":formatting-prefs"), sexp(
        key(":preserveDanglingCloseParenthesis"), true,
        key(":alignParameters"), true,
        key(":doubleIndentClassDeclaration"), false
      )
    )
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(appSettings: _*)
}
