package play.db.ebean.dbmigration

import java.sql.SQLException
import javax.inject.{Inject, Singleton}

import io.ebean.Ebean
import io.ebean.dbmigration.MigrationConfig
import io.ebean.dbmigration.runner._
import io.ebean.dbmigration.util.JdbcClose
import play.api.{Configuration, Environment, Logger, Mode}
import play.core.WebCommands

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * PlayInitializer.
  *
  * @since 17.01.29
  * @author Thibault Meyer
  */
@Singleton
class PlayInitializer @Inject()
(configuration: Configuration, environment: Environment, webCommands: WebCommands) {

  /**
    * @since 17.01.30
    */
  private val isEnabled: Boolean = configuration.getBoolean(
    "ebean.dbmigration.enabled"
  ).getOrElse(false)

  /**
    * @since 17.01.29
    */
  private val migrationPath: String = configuration.getString(
    "ebean.dbmigration.migrationPath"
  ).getOrElse("dbmigration")

  /**
    * @since 17.01.30
    */
  private val autoApply: Boolean = configuration.getBoolean(
    "ebean.dbmigration.autoApply"
  ).getOrElse(false)

  /**
    * @since 17.01.29
    */
  private val allowAlreadyProcessedFiles = environment.mode == Mode.Dev

  /**
    * Ebean migration initialization.
    *
    * @since 17.01.29
    */
  def onStart(): Unit = {
    if (this.isEnabled) {
      val maybeSubKeys = configuration.getConfig("ebean.servers")
      if (maybeSubKeys.isDefined) maybeSubKeys.get.subKeys.foreach(key => {
        val changedMigrationResource = this.checkServerState(key)
        if (changedMigrationResource.nonEmpty) {
          var data = ""
          for (res <- changedMigrationResource) {
            data +=
              s"""▅▆▇█ ${res.getLocation.split("/").takeRight(1).apply(0)} █▇▆▅
                 |${res.getContent}
                 |
                        |
                        |""".stripMargin
          }
          if (data.nonEmpty) {
            val ebeanMigrationWC = new EbeanMigrationWebCommand(this.configuration, this.environment)
            webCommands.addHandler(ebeanMigrationWC)
            val forceFrom = changedMigrationResource.toStream.find(k => !k.isRepeatable).map(k => k.key())
            if (this.autoApply) {
              Logger.info(s"Applying migration on database '$key'")
              EbeanToolbox.migrateEbeanServer(
                this.migrationPath,
                this.environment,
                key,
                if (this.allowAlreadyProcessedFiles) forceFrom.orNull else null,
                this.allowAlreadyProcessedFiles
              )
            } else {
              if (this.environment.mode == Mode.Prod) {
                throw InvalidDatabaseStateProduction(key)
              }
              throw InvalidDatabaseState(key, data, if (this.allowAlreadyProcessedFiles) forceFrom.orNull else null)
            }
          }
        }
      })
    }
  }

  /**
    * Get the list of changed resources for the given Ebean server.
    *
    * @param serverName The Ebean server name
    * @return A list of changed resources
    * @since 17.01.29
    */
  private[this] def checkServerState(serverName: String): List[LocalMigrationResource] = {
    var changedMigrationResource = ListBuffer[LocalMigrationResource]()
    var dataFromMigrationTable = Map[String, Int]()

    val ebeanConnection = Ebean.getServer(serverName)
      .getPluginApi
      .getDataSource
      .getConnection
    try {
      val preparedStatement = ebeanConnection.prepareStatement(
        "SELECT mversion, mchecksum FROM db_migration ORDER BY id"
      )
      val resultSet = preparedStatement.executeQuery()
      try {
        while (resultSet.next()) {
          dataFromMigrationTable += resultSet.getString(1) -> resultSet.getInt(2)
        }
      } finally {
        JdbcClose.close(resultSet)
      }
    } catch {
      case ex: SQLException =>
        if (!ex.getMessage.contains("exist")) {
          throw ex
        }
    } finally {
      JdbcClose.close(ebeanConnection)
    }

    val migrationConfig: MigrationConfig = new MigrationConfig
    if (environment.getFile(s"conf/$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-${environment.mode.toString.toLowerCase}").isDirectory) {
      migrationConfig.setMigrationPath(s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName-${environment.mode.toString.toLowerCase}")
    } else if (environment.getFile(s"conf/$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName").isDirectory) {
      migrationConfig.setMigrationPath(s"$migrationPath${if (!migrationPath.endsWith("/")) "/"}$serverName")
    } else {
      return changedMigrationResource.toList
    }
    val migrationResources = new LocalMigrationResources(migrationConfig)
    if (migrationResources.readResources()) {
      migrationResources.getVersions.toStream.foreach(res => {
        val maybeChecksum = dataFromMigrationTable.get(res.key())
        if (maybeChecksum.isEmpty
          || (maybeChecksum.get != EbeanToolbox.calculateCRC32(res.getContent)
          && (res.isRepeatable || this.allowAlreadyProcessedFiles))) {
          changedMigrationResource += res
        }
      })
    }

    changedMigrationResource.toList
  }

  /**
    * Run.
    *
    * @since 17.01.29
    */
  onStart()
}
