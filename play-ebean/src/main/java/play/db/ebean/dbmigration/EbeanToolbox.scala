package play.db.ebean.dbmigration

import java.io.{BufferedReader, StringReader}
import java.sql.SQLException
import java.util.zip.CRC32
import javax.persistence.PersistenceException

import io.ebean.Ebean
import io.ebean.dbmigration.{MigrationConfig, MigrationRunner}
import play.api.Environment

/**
  * EbeanToolbox
  *
  * @since 17.01.30
  * @author Thibault Meyer
  */
object EbeanToolbox {

  /**
    * Compute the CRC32.
    *
    * @param str The data to crunch
    * @return The CRC32 value
    * @since 17.01.30
    */
  def calculateCRC32(str: String): Int = {
    val crc32 = new CRC32()
    val bufferedReader = new BufferedReader(new StringReader(str))
    var line = ""
    do {
      line = bufferedReader.readLine()
      if (line != null) {
        crc32.update(line.getBytes("UTF-8"))
      }
    } while (line != null)
    crc32.getValue.asInstanceOf[Int]
  }

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

    if (forceKey != null && forceKey.trim.nonEmpty && allowAlreadyProcessedFiles) {
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

    val migrationConfig: MigrationConfig = new MigrationConfig
    if (environment.getFile(s"conf/$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-${environment.mode.toString.toLowerCase}").isDirectory) {
      migrationConfig.setMigrationPath(s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-${environment.mode.toString.toLowerCase}")
    } else {
      migrationConfig.setMigrationPath(s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName")
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
