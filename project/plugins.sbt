resolvers ++= Seq(
  DefaultMavenRepository,
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.file("Local Play20 Repository", file("/opt/git-repos/play20/repository/local"))(Resolver.ivyStylePatterns)
)

libraryDependencies <+= sbtVersion(v => "com.mojolly.scalate" %% "xsbt-scalate-generator" % (v + "-0.1.5"))

addSbtPlugin("play" % "sbt-plugin" % "2.0-RC1-SNAPSHOT")
