package play.db.ebean.dbmigration

import java.sql.SQLException
import javax.persistence.PersistenceException

import io.ebean.Ebean
import io.ebean.migration.runner.LocalMigrationResource
import io.ebean.migration.{MigrationConfig, MigrationRunner}
import play.api.Environment

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
    * @param migrationPath              Migration files root path
    * @param environment                The current environment
    * @param serverName                 The Ebean server name
    * @param forceKey                   The script key from which force migration
    * @param allowAlreadyProcessedFiles Is processing already processed files is allowed?
    * @throws MigrationRunnerError If something goes wrong during migration
    */
  def migrateEbeanServer(migrationPath: String, environment: Environment, serverName: String, forceKey: String, allowAlreadyProcessedFiles: Boolean): Unit = {
    val ebeanServer = Ebean.getServer(serverName)

    if (forceKey != null && forceKey.trim.nonEmpty && allowAlreadyProcessedFiles) { //TODO: Remove?
      try {
        ebeanServer
          .createSqlUpdate(s"DELETE FROM db_migration WHERE id >= (SELECT MAX(id) FROM db_migration WHERE mversion LIKE '${forceKey.trim.replace("%", "")}')")
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
      serverName,
      ebeanServer.getPluginApi.getPluginApi.getDatabasePlatform.getPlatform.name.toLowerCase
    )
    if (folder.isDefined) {
      val migrationConfig: MigrationConfig = new MigrationConfig
      migrationConfig.setMigrationPath(folder.get)
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
    * @param migrationPath Migration files root path
    * @param environment   The current environment
    * @param serverName    The Ebean server name
    * @throws MigrationRunnerError If something goes wrong during migration
    */
  def checkEbeanServerState(migrationPath: String, environment: Environment, serverName: String): Iterable[LocalMigrationResource] = {
    val ebeanServer = Ebean.getServer(serverName)
    val folder = guessMigrationFolderToUse(
      migrationPath,
      environment,
      serverName,
      ebeanServer.getPluginApi.getPluginApi.getDatabasePlatform.getPlatform.name.toLowerCase
    )
    if (folder.isDefined) {
      val migrationConfig: MigrationConfig = new MigrationConfig
      migrationConfig.setMigrationPath(folder.get)
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
    * Try to guess the migration folder to use.
    *
    * @since 17.06.07
    * @return The migration folder to use
    */
  private[this] def guessMigrationFolderToUse(migrationPath: String, environment: Environment,
                                              serverName: String, plateformName: String): Option[String] = {
    val folderToTry = List[String](
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$plateformName/$serverName-${environment.mode.toString.toLowerCase}",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$plateformName/$serverName",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-${environment.mode.toString.toLowerCase}",
      s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName"
    )
    folderToTry
      .filter(folder => environment.getFile("conf/" + folder).isDirectory)
      .lift(0)
  }
}
