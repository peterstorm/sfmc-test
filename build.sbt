val Scala3Version = "3.0.2"
val CatsEffectVersion = "3.2.3"
val CatsVersion = "2.6.1"
val CatsParseVersion = "0.3.4"
val CirceVersion = "0.14.1"
val Fs2Version = "3.1.1"
val Http4sVersion = "1.0.0-M24"
val DoobieVersion = "1.0.0-M5"
val PureConfigVersion = "0.16.0"
val Log4CatsVersion = "2.1.1"
val LogBackVersion = "1.2.3"
val MunitVersion = "0.7.28"
val MunitCatsEffectVersion = "1.0.6"
val ScalaCheckEffectVersion = "1.0.2"
val TestContainersScalaVersion = "0.39.5"
val FlywayVersion = "7.10.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    name := "sfmc-test",
    ThisBuild / run / fork := true,
    Test / fork := true,
    scalaVersion := Scala3Version,
    ThisBuild / version ~= (_.replace('+', '-')),
    scalacOptions += "-Ykind-projector",

    Docker / packageName := "sfmc-test",
    dockerUpdateLatest := true,
    dockerUsername := Some("peterstormio"),
    dockerBaseImage := "openjdk:8-jdk-alpine",
    makeBatScripts := Seq(),

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %% "cats-parse" % CatsParseVersion,
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-extras" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.circe" %% "circe-jawn" % CirceVersion,
      "co.fs2" %% "fs2-core" % Fs2Version,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.typelevel" %% "log4cats-slf4j" % Log4CatsVersion,
      "ch.qos.logback" % "logback-classic" % LogBackVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "com.github.pureconfig" %% "pureconfig-core" % PureConfigVersion,
      "org.flywaydb" % "flyway-core" % FlywayVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-munit" % TestContainersScalaVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-oracle-xe" % TestContainersScalaVersion % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % ScalaCheckEffectVersion,
      "org.scalameta" %% "munit-scalacheck" % MunitVersion,
      //"com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion
    ),
  )
