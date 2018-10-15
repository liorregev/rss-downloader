name := "rss-downloader"

version := "0.1"

scalaVersion := "2.12.7"

javaOptions ++= Seq("-Xms512M", "-Xmx8192M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

scalacOptions ++= Seq(
  "-feature", "-deprecation", "-unchecked", "-explaintypes",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-language:reflectiveCalls", "-language:implicitConversions", "-language:postfixOps", "-language:existentials",
  "-language:higherKinds",
  // http://blog.threatstack.com/useful-scala-compiler-options-part-3-linting
  "-Xcheckinit", "-Xexperimental", "-Xfatal-warnings", /*"-Xlog-implicits", */"-Xfuture", "-Xlint",
  "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-numeric-widen", "-Yno-adapted-args", "-Ywarn-unused-import",
  "-Ywarn-unused", "-Ypartial-unification"
)

wartremoverErrors ++= Seq(
  Wart.StringPlusAny, Wart.FinalCaseClass, Wart.JavaConversions, Wart.Null, Wart.Product, Wart.Serializable,
  Wart.LeakingSealed, Wart.While, Wart.Return, Wart.ExplicitImplicitTypes, Wart.Enumeration, Wart.FinalVal,
  Wart.TryPartial, Wart.TraversableOps, Wart.OptionPartial, ContribWart.SomeApply
)

wartremoverWarnings ++= wartremover.Warts.allBut(
  Wart.Nothing, Wart.DefaultArguments, Wart.Throw, Wart.MutableDataStructures, Wart.NonUnitStatements, Wart.Overloading,
  Wart.Option2Iterable, Wart.ImplicitConversion, Wart.ImplicitParameter, Wart.Recursion,
  Wart.Any, Wart.Equals, // Too many warnings because of spark's Row
  Wart.AsInstanceOf, // Too many warnings because of bad DI practices
  Wart.ArrayEquals // Too many warnings because we're using byte arrays in Spark
)


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
  "com.github.scopt"   %% "scopt"                    % "3.7.0",
  "org.slf4j"           % "slf4j-api"                % "1.7.25",
  "ch.qos.logback"      % "logback-classic"          % "1.2.3",
  "org.scalatest"      %% "scalatest"                % "3.0.5" % Test
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)
assemblyMergeStrategy in assembly := {
  case x if x.endsWith("application.conf") => MergeStrategy.first
  case x if x.endsWith(".class") => MergeStrategy.last
  case x if x.endsWith("logback.xml") => MergeStrategy.first
  case x if x.endsWith("version.properties") => MergeStrategy.concat
  case x if x.endsWith(".properties") => MergeStrategy.last
  case x if x.contains("/resources/") => MergeStrategy.last
  case x if x.startsWith("META-INF/mailcap") => MergeStrategy.last
  case x if x.startsWith("META-INF/mimetypes.default") => MergeStrategy.first
  case x if x.startsWith("META-INF/maven/org.slf4j/slf4j-api/pom.") => MergeStrategy.first
  case x if x.startsWith("CHANGELOG.") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    if (oldStrategy == MergeStrategy.deduplicate)
      MergeStrategy.first
    else
      oldStrategy(x)
}