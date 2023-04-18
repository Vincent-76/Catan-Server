name := """Catan-Web"""
organization := "com.aimit"
version := "1.0-SNAPSHOT"

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

scalaVersion := "2.13.10"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb)

resolvers += Resolver.githubPackages("Vincent-76", "Catan")

libraryDependencies += "com.aimit" %% "htwg-catan-lib" % "0.1.1"
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
dependencyOverrides += "org.scala-lang.modules" % "scala-xml" % "2.1.0"

Assets / LessKeys.less / includeFilter := "*.less"
Assets / LessKeys.less / excludeFilter := "_*.less"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.aimit.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.aimit.binders._"
