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
  "com.typesafe.play"  %% "play-ahc-ws-standalone"   % playWsStandaloneVersion,
  "com.typesafe.play"  %% "play-ws-standalone-json"  % playWsStandaloneVersion,
  "com.typesafe.slick" %% "slick"                    % "3.2.3",
  "com.typesafe.slick" %% "slick-hikaricp"           % "3.2.3",
  "org.slf4j"           % "slf4j-nop"                % "1.6.4",
  "com.chuusai"        %% "shapeless"                % "2.3.3",
  "org.typelevel"      %% "cats-core"                % "1.4.0",
  "com.typesafe.play"  %% "play-json"                % "2.6.10",
  "com.rometools"       % "rome"                     % "1.11.0",
  "mysql"               % "mysql-connector-java"     % "5.1.47",
  "org.scalatest"      %% "scalatest"                % "3.0.5" % Test
)