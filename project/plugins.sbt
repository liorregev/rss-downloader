logLevel := Level.Warn

resolvers += Resolver.bintrayRepo("sbt", "sbt-plugin-releases")
resolvers += Resolver.url("sbts3 ivy resolver", url("https://dl.bintray.com/emersonloureiro/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("cf.janga" % "sbts3" % "0.10.3")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5" exclude("org.apache.maven", "maven-plugin-api"))
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3") // Run with `sbt dependencyUpdates`
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1") // https://github.com/puffnfresh/wartremover/issues/294
addSbtPlugin("org.wartremover" % "sbt-wartremover-contrib" % "1.1.0")
addSbtPlugin("com.softwaremill.clippy" % "plugin-sbt" % "0.5.3")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC13")
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "0.2.0")