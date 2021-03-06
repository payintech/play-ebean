import com.typesafe.sbt.SbtPgp.autoImportImpl.usePgpKeyHex
import interplay.ScalaVersions.scala212
import sbt.Keys.{publishMavenStyle, publishTo}
import sbt.inc.Analysis

val PlayVersion = playVersion(sys.props.getOrElse("play.version", "2.8.0"))
val PlayEnhancerVersion = "1.2.2"
val EbeanVersion = "12.7.1"
val EbeanAgentVersion = "12.7.1"
val EbeanDDLGenerator = "12.7.1"
val EbeanDBMigrationVersion = "12.4.0"
val TypesafeConfigVersion = "1.4.1"
val scala213 = "2.13.4"

lazy val root = project
  .in(file("."))
  .enablePlugins(PlayRootProject, CrossPerProjectPlugin)
  .aggregate(core)
  .settings(
    name := "play-ebean-root",
    description := "Play Ebean module",
    organization := "com.payintech",
    homepage := Some(url(s"https://github.com/payintech/play-ebean")),
    releaseCrossBuild := false,
    publishMavenStyle := false
  )
  .settings(
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      System.getenv("OSS_ST_USERNAME"),
      System.getenv("OSS_ST_PASSWORD")
    ),
    useGpg := true,
    useGpgAgent := true,
    usePgpKeyHex("B4B939B5"),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := <licenses>
      <license>
        <name>Apache License 2.0</name>
        <url>https://opensource.org/licenses/Apache-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
      <scm>
        <url>git@github.com:payintech/play-ebean.git</url>
        <connection>scm:git:git@github.com:payintech/play-ebean.git</connection>
      </scm>
      <developers>
        <developer>
          <id>payintech</id>
          <name>PayinTech Team</name>
          <url>https://github.com/payintech</url>
        </developer>
      </developers>
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(Playdoc, PlayLibrary)
  .settings(
    name := "play-ebean",
    description := "Play Ebean module",
    organization := "com.payintech",
    homepage := Some(url(s"https://github.com/payintech/play-ebean")),
    crossScalaVersions := Seq(scala212, scala213),
    libraryDependencies ++= playEbeanDeps,
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "play/db/ebean/**"
    )
  )
  .settings(
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      System.getenv("OSS_ST_USERNAME"),
      System.getenv("OSS_ST_PASSWORD")
    ),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    useGpg := true,
    useGpgAgent := true,
    usePgpKeyHex("B4B939B5"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := <scm>
      <url>git@github.com:payintech/play-ebean.git</url>
      <connection>scm:git:git@github.com:payintech/play-ebean.git</connection>
    </scm>
      <developers>
        <developer>
          <id>playframework</id>
          <name>Play Framework Team</name>
          <url>https://github.com/playframework</url>
        </developer>
        <developer>
          <id>payintech</id>
          <name>PayinTech Team</name>
          <url>https://github.com/payintech</url>
        </developer>
      </developers>
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(PlaySbtPlugin)
  .settings(
    name := "sbt-play-ebean",
    description := "Play Ebean module",
    organization := "com.payintech",
    homepage := Some(url(s"https://github.com/payintech/play-ebean")),
    libraryDependencies ++= sbtPlayEbeanDeps,
    libraryDependencies ++= Seq(
      sbtPluginDep("com.typesafe.sbt" % "sbt-play-enhancer" % PlayEnhancerVersion, (sbtVersion in pluginCrossBuild).value, scalaVersion.value),
      sbtPluginDep("com.typesafe.play" % "sbt-plugin" % PlayVersion, (sbtVersion in pluginCrossBuild).value, scalaVersion.value)
    ),
    resourceGenerators in Compile += generateVersionFile.taskValue,
    scriptedLaunchOpts ++= Seq("-Dplay-ebean.version=" + version.value),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (publishLocal in core).value
    }
  )
  .settings(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    useGpg := true,
    usePgpKeyHex("B4B939B5"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := <scm>
      <url>git@github.com:payintech/play-ebean.git</url>
      <connection>scm:git:git@github.com:payintech/play-ebean.git</connection>
    </scm>
      <developers>
        <developer>
          <id>playframework</id>
          <name>Play Framework Team</name>
          <url>https://github.com/playframework</url>
        </developer>
        <developer>
          <id>payintech</id>
          <name>PayinTech Team</name>
          <url>https://github.com/payintech</url>
        </developer>
      </developers>
  )

playBuildRepoName in ThisBuild := "play-ebean"

playBuildExtraPublish := {
  (PgpKeys.publishSigned in plugin).value
}

lazy val reflectionDeps = Seq(
  ("org.reflections" % "reflections" % "0.9.11")
    .exclude("com.google.code.findbugs", "annotations")
    .classifier("")
)

// Dependencies
def playEbeanDeps = Seq(
  "com.typesafe.play" %% "play-guice" % PlayVersion,
  "com.typesafe.play" %% "play-java-jdbc" % PlayVersion,
  "com.typesafe.play" %% "play-jdbc-evolutions" % PlayVersion,
  "io.ebean" % "ebean" % EbeanVersion,
  "io.ebean" % "ebean-agent" % EbeanAgentVersion,
  "io.ebean" % "ebean-migration" % EbeanDBMigrationVersion,
  "io.ebean" % "ebean-ddl-generator" % EbeanDDLGenerator,
  "com.typesafe.play" %% "play-test" % PlayVersion % Test
) ++ reflectionDeps

def sbtPlayEbeanDeps = Seq(
  "io.ebean" % "ebean-agent" % EbeanAgentVersion,
  "com.typesafe" % "config" % TypesafeConfigVersion
)

// sbt deps
def sbtPluginDep(moduleId: ModuleID, sbtVersion: String, scalaVersion: String) = {
  Defaults.sbtPluginExtra(moduleId, CrossVersion.binarySbtVersion(sbtVersion), CrossVersion.binaryScalaVersion(scalaVersion))
}

// Ebean enhancement
def enhanceEbeanClasses(classpath: Classpath, analysis: Analysis, classDirectory: File, pkg: String): Analysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl.loadClass("io.ebean.enhance.Transformer").getConstructor(classOf[ClassLoader], classOf[String]).newInstance(cl, "debug=0").asInstanceOf[AnyRef]
  val ft = cl.loadClass("io.ebean.enhance.ant.OfflineFileTransform").getConstructor(
    t.getClass, classOf[ClassLoader], classOf[String]
  ).newInstance(t, ClassLoader.getSystemClassLoader, classDirectory.getAbsolutePath).asInstanceOf[AnyRef]
  ft.getClass.getDeclaredMethod("process", classOf[String]).invoke(ft, pkg)
  analysis
}

// Version file
def generateVersionFile = Def.task {
  val version = (Keys.version in core).value
  val file = (resourceManaged in Compile).value / "play-ebean.version.properties"
  val content = s"play-ebean.version=$version"
  IO.write(file, content)
  Seq(file)
}
