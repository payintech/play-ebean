package play.db.ebean.dbmigration

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Logger, Mode}
import play.core.WebCommands

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
  private val isEnabled: Boolean = configuration.getOptional[Boolean](
    "ebean.dbmigration.enabled"
  ).getOrElse(false)

  /**
    * @since 17.01.29
    */
  private val migrationPath: String = configuration.getOptional[String](
    "ebean.dbmigration.migrationPath"
  ).getOrElse("dbmigration")

  /**
    * @since 17.01.30
    */
  private val autoApply: Boolean = configuration.getOptional[Boolean](
    "ebean.dbmigration.autoApply"
  ).getOrElse(false)

  /**
    * @since 18.03.07
    */
  private val platformName: String = configuration.getOptional[String](
    "ebean.dbmigration.platformName"
  ).orNull

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
      val maybeSubKeys = configuration.getOptional[Configuration]("ebean.servers")
      if (maybeSubKeys.isDefined) maybeSubKeys.get.subKeys.foreach(key => {
        val changedMigrationResource = EbeanToolbox.checkEbeanServerState(
          this.platformName,
          this.migrationPath,
          this.environment,
          key
        )
        if (changedMigrationResource.nonEmpty) {
          var data = ""
          for (res <- changedMigrationResource) {
            data = data.concat(
              s"""▅▆▇█ ${res.getLocation.split("/").takeRight(1).apply(0)} █▇▆▅
                 |${res.getContent}
                 |
                        |
                        |""".stripMargin
            )
          }
          if (data.nonEmpty) {
            val ebeanMigrationWC = new EbeanMigrationWebCommand(this.configuration, this.environment)
            webCommands.addHandler(ebeanMigrationWC)
            val forceFrom = changedMigrationResource.toStream.find(k => !k.isRepeatable).map(k => k.key())
            if (this.autoApply) {
              Logger.info(s"Applying migration on database '$key'")
              EbeanToolbox.migrateEbeanServer(
                this.platformName,
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
    * Run.
    *
    * @since 17.01.29
    */
  onStart()
}
