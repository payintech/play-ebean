package play.db.ebean.dbmigration

import play.api._

/**
  * InvalidDatabaseStateProduction.
  *
  * @param db The database name
  * @since 17.01.30
  * @author Thibault Meyer
  */
case class InvalidDatabaseStateProduction(db: String) extends PlayException.RichDescription(
  "Database '" + db + "' needs migration!", "Ebean DB Migration") {

  def subTitle: String = "Please check the following key is present in your configuration file"

  def content: String =
    """ebean {
      |    dbmigration {
      |        autoApply = true
      |    }
      |}""".stripMargin

  def htmlDescription: String = "<span>You have to apply migration to continue</span>"
}
