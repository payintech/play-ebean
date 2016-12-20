name := "play-sbt-ebean-test"

version := "TEST-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  "org.avaje.ebean" % "ebean-elastic" % "1.5.1"
)

scalaVersion := "2.11.8"
