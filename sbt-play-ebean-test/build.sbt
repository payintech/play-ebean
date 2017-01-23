name := "play-sbt-ebean-test"

version := "TEST-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  "io.ebean" % "ebean-elastic" % "2.1.1",
  "io.ebean" % "ebean-cluster" % "2.1.1"
)

scalaVersion := "2.11.8"
