package play.db.ebean.dbmigration

import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Environment, Mode}
import play.core.{BuildLink, HandleWebCommandSupport}

/**
  * EbeanMigrationWebCommand
  *
  * @since 17.01.29
  * @author Thibault Meyer
  * @see HandleWebCommandSupport
  */
class EbeanMigrationWebCommand(configuration: Configuration, environment: Environment)
  extends HandleWebCommandSupport {

  /**
    * @since 17.01.29
    */
  private val migrationPath: String = configuration.getOptional[String](
    "ebean.dbmigration.migrationPath"
  ).getOrElse("dbmigration")

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
    *
    * @param request The current request
    * @param sbtLink Handle to communicate with embedded Play server
    * @param path    Location of the Play application
    * @return Maybe a Result
    * @since 17.01.29
    */
  def handleWebCommand(request: RequestHeader, sbtLink: BuildLink, path: java.io.File): Option[Result] = {
    request.path match {
      case EbeanMigrationWebPath.migratePath(serverName) =>
        EbeanToolbox.migrateEbeanServer(
          this.platformName,
          this.migrationPath,
          this.environment,
          serverName,
          this.getForceMigrationFrom(request),
          this.allowAlreadyProcessedFiles
        )
        sbtLink.forceReload()
        Some(Redirect(this.getRedirectUrlFromRequest(request)))
      case _ =>
        None
    }
  }

  /**
    * Retrieve the key of the first script from which
    * user want force migration.
    *
    * @param request The current request
    * @return The script key
    * @since 17.01.29
    */
  private[this] def getForceMigrationFrom(request: RequestHeader): String = (for {
    urls <- request.queryString.get("force")
    url <- urls.headOption
  } yield url).orNull

  /**
    * Retrieve the URL to use as redirection.
    *
    * @param request The current request
    * @return The URL
    * @since 17.01.29
    */
  private[this] def getRedirectUrlFromRequest(request: RequestHeader): String = {
    (for {
      urls <- request.queryString.get("redirect")
      url <- urls.headOption
    } yield url).getOrElse("/")
  }
}
