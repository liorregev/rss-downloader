name := "rss-downloader"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

val playWsStandaloneVersion = "2.0.0-M6"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone"   % playWsStandaloneVersion,
  "com.typesafe.play" %% "play-ws-standalone-json"  % playWsStandaloneVersion,
  "com.chuusai"       %% "shapeless"                % "2.3.3",
  "org.typelevel"     %% "cats-core"                % "1.4.0",
  "com.typesafe.play" %% "play-json"                % "2.6.10",
  "com.rometools"      % "rome"                     % "1.11.0",
  "mysql"              % "mysql-connector-java"     % "8.0.12",
  "org.scalatest"     %% "scalatest"                % "3.0.5" % Test
)