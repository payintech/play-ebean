package play.db.ebean.dbmigration

import java.sql.SQLException

import play.api.PlayException

/**
  * InvalidDatabaseRevision.
  *
  * @param db           The database name
  * @param sqlException The SQL exception
  * @since 17.01.29
  * @author Thibault Meyer
  */
case class MigrationRunnerError(db: String, sqlException: SQLException) extends PlayException.RichDescription(
  "Migration error on database '" + db + "'", "Ebean DB Migration", sqlException) {

  def subTitle: String = "Error details"

  def content: String =
    s"""Database: $db
       |SQLState: ${if (sqlException.getSQLState != null) sqlException.getSQLState else "-"}
       |Error Code: ${sqlException.getErrorCode}""".stripMargin

  def htmlDescription: String = s"<span style=#word-wrap:break-word;'>${sqlException.getMessage}</span>".stripMargin
}
