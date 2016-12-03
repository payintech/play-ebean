name := "play-sbt-ebean-test"

version := "16.11"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.8"
