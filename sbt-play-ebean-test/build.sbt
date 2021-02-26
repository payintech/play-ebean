name := "play-sbt-ebean-test"

version := "TEST-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

val javaVersion = settingKey[String]("The version of Java used for building.")
javaVersion := System.getProperty("java.version")

val java9UpDependencies: Seq[sbt.ModuleID] =
  if (!javaVersion.toString.startsWith("1.8")) {
    Seq(
      "com.sun.activation" % "javax.activation" % "1.2.0",
      "com.sun.xml.bind" % "jaxb-core" % "2.3.0",
      "com.sun.xml.bind" % "jaxb-impl" % "2.3.1",
      "javax.jws" % "javax.jws-api" % "1.1",
      "javax.xml.bind" % "jaxb-api" % "2.3.0",
      "javax.xml.ws" % "jaxws-api" % "2.3.1"
    )
  } else {
    Seq.empty
  }

libraryDependencies ++= Seq(
  guice,
  "com.h2database" % "h2" % "1.4.196"
) ++ java9UpDependencies

scalaVersion := "2.13.4"
