package play.db.ebean.dbmigration

import play.api._

/**
  * InvalidDatabaseRevision.
  *
  * @param db        The database name
  * @param script    The SQL scripts
  * @param forceFrom From which version force migration
  * @since 17.01.29
  * @author Thibault Meyer
  */
case class InvalidDatabaseState(db: String, script: String, forceFrom: String) extends PlayException.RichDescription(
  "Database '" + db + "' needs migration!", "Ebean DB Migration") {

  private val redirectToApply = s"document.location = '${EbeanMigrationWebPath.migratePath(db)}?redirect=' + encodeURIComponent(location);"

  private val redirectToForceApply = s"document.location = '${EbeanMigrationWebPath.migratePath(db)}?force=$forceFrom&redirect=' + encodeURIComponent(location);"

  def subTitle: String = "The following SQL instructions will be run:"

  def content: String = script

  def htmlDescription: String =
    s"""<span>
       |    Click on the button 'Migrate' to apply needed modifications.
       |    <br/>
       |    <br/>
       |    <input name="evolution-button" type="button" value="Migrate" onclick="$redirectToApply"/>
       |    ${if (forceFrom != null) s"""<input name="evolution-button" type="button" value="Force migrate" onclick="$redirectToForceApply"/>""" else ""}
       |</span>""".stripMargin
}
