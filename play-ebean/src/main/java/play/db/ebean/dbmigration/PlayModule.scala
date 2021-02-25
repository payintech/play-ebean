package play.db.ebean.dbmigration

import org.slf4j.{Logger, LoggerFactory}
import play.api.inject._
import play.api.{Configuration, Environment}

/**
  * PlayModule.
  *
  * @since 17.01.29
  * @author Thibault Meyer
  */
class PlayModule extends Module {

  /**
    * @since 20.02.25
    */
  private val logger: Logger = LoggerFactory.getLogger(this.getClass);

  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[PlayInitializer]] = {
    this.logger.trace("Loading module DBMigration")
    Seq(
      bind[PlayInitializer].toSelf.eagerly
    )
  }
}
