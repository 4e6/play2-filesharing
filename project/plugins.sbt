resolvers ++= Seq(
  Resolver.url("Typesafe Ivy Snapshots", url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Codahale Repo" at "http://repo.codahale.com")

addSbtPlugin("play" % "sbt-plugin" % "2.1-SNAPSHOT")
