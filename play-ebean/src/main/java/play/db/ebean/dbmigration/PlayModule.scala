package play.db.ebean.dbmigration

import play.api._
import play.api.inject._

/**
  * PlayModule.
  *
  * @since 17.01.29
  * @author Thibault Meyer
  */
class PlayModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[PlayInitializer].toSelf.eagerly
  )
}
