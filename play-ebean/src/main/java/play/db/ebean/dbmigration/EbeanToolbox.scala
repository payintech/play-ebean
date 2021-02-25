package play.db.ebean.dbmigration

import io.ebean.migration.runner.LocalMigrationResource
import io.ebean.migration.{MigrationConfig, MigrationRunner}
import io.ebean.{DB, Ebean}
import play.api.{Environment, Mode}

import java.sql.SQLException
import javax.persistence.PersistenceException
import scala.collection.JavaConverters._

/**
  * EbeanToolbox
  *
  * @since 17.01.30
  * @author Thibault Meyer
  */
object EbeanToolbox {

  /**
    * Migrate the given Ebean server.
    *
    * @since 17.01.30
    * @param platformName               The platform name
    * @param migrationPath              Migration files root path
    * @param environment                The current environment
    * @param serverName                 The Ebean server name
    * @param forceKey                   The script key from which force migration
    * @param allowAlreadyProcessedFiles Is processing already processed files is allowed?
    * @throws MigrationRunnerError If something goes wrong during migration
    */
  def migrateEbeanServer(platformName: String, migrationPath: String, environment: Environment,
                         serverName: String, forceKey: String, allowAlreadyProcessedFiles: Boolean): Unit = {
    val ebeanServer = DB.byName(serverName)

    if (forceKey != null && forceKey.trim.nonEmpty && allowAlreadyProcessedFiles) { //TODO: Remove?
      try {
        ebeanServer
          .sqlUpdate(s"DELETE FROM db_migration WHERE id >= (SELECT MAX(id) FROM db_migration WHERE mversion LIKE '${forceKey.trim.replace("%", "")}')")
          .execute()
      } catch {
        case ex@(_: PersistenceException | _: SQLException) =>
          if (!ex.getMessage.contains("exist")) {
            throw ex
          }
      }
    }

    val folder = guessMigrationFolderToUse(
      migrationPath,
      environment,
      guessModeToUse(environment.mode),
      serverName,
      ebeanServer.getPluginApi.getPluginApi.getDatabasePlatform.getPlatform.name.toLowerCase
    )
    if (folder.isDefined) {
      val migrationConfig: MigrationConfig = new MigrationConfig
      migrationConfig.setMigrationPath(folder.get)
      if (platformName != null && platformName.nonEmpty) {
        migrationConfig.setPlatformName(platformName)
      }
      val migrationRunner = new MigrationRunner(migrationConfig)
      try {
        migrationRunner.run(
          ebeanServer
            .getPluginApi
            .getDataSource
        )
      } catch {
        case e: SQLException =>
          throw MigrationRunnerError(serverName, e)
        case e: RuntimeException =>
          throw MigrationRunnerError(serverName, e.getCause.asInstanceOf[SQLException])
      }
    }
  }

  /**
    * Migrate the given Ebean server.
    *
    * @since 17.06.07
    * @param platformName    The platform name
    * @param migrationPath   Migration files root path
    * @param playEnvironment The current Play environment
    * @param serverName      The Ebean server name
    * @throws MigrationRunnerError If something goes wrong during migration
    */
  def checkEbeanServerState(platformName: String, migrationPath: String, playEnvironment: Environment,
                            serverName: String): Iterable[LocalMigrationResource] = {
    val ebeanServer = Ebean.getServer(serverName)
    val folder = guessMigrationFolderToUse(
      migrationPath,
      playEnvironment,
      guessModeToUse(playEnvironment.mode),
      serverName,
      ebeanServer.getPluginApi.getPluginApi.getDatabasePlatform.getPlatform.name.toLowerCase
    )
    if (folder.isDefined) {
      val migrationConfig: MigrationConfig = new MigrationConfig
      migrationConfig.setMigrationPath(folder.get)
      if (platformName != null && platformName.nonEmpty) {
        migrationConfig.setPlatformName(platformName)
      }
      val migrationRunner = new MigrationRunner(migrationConfig)
      try {
        migrationRunner.checkState(
          ebeanServer
            .getPluginApi
            .getDataSource
        ).asScala
      } catch {
        case e: SQLException =>
          throw MigrationRunnerError(serverName, e)
        case e: RuntimeException =>
          throw MigrationRunnerError(serverName, e.getCause.asInstanceOf[SQLException])
      }
    } else {
      Iterable[LocalMigrationResource]()
    }
  }

  /**
    * Resolve mode to use. By instance, Play can run in Prod mode
    * but user want run Test migration SQL scripts.
    *
    * @param playMode The current Play running mode
    * @return The mode to use
    * @since 18.01.13
    */
  private[this] def guessModeToUse(playMode: Mode): String = sys.env.getOrElse(
    "EBEAN_MIGRATION_MODE",
    playMode.toString
  ).toLowerCase.replaceAll("[^a-z0-9]", "")

  /**
    * Try to guess the migration folder to use.
    *
    * @since 17.06.07
    * @param migrationPath The migration path
    * @param environment   The current Play environment
    * @param mode          The migration mode
    * @param serverName    The Ebean server name
    * @param platformName  The current platform (ie: PostgreSQL)
    * @return The migration folder to use
    */
  private[this] def guessMigrationFolderToUse(migrationPath: String, environment: Environment, mode: String,
                                              serverName: String, platformName: String): Option[String] = {
    val folderToTry = List[String](
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$platformName/$serverName-$mode",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$platformName/$serverName",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-$mode",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName"
    )
    folderToTry.find(folder => environment.getFile("conf/" + folder).isDirectory)
  }
}
