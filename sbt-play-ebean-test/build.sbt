name := "play-sbt-ebean-test"

version := "TEST-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  guice,
  "com.h2database" % "h2" % "1.4.196",
  "io.ebean" % "ebean-elastic" % "11.0.1-RC"
)

scalaVersion := "2.12.1"
