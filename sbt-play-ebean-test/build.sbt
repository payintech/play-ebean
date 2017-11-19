name := "play-sbt-ebean-test"

version := "TEST-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.196",
  "io.ebean" % "ebean-elastic" % "11.0.1-RC",
  "io.ebean" % "ebean-cluster" % "2.1.1"
)

scalaVersion := "2.12.1"
