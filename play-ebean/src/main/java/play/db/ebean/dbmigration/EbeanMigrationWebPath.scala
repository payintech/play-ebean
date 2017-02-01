package play.db.ebean.dbmigration

/**
  * EbeanMigrationWebPath
  *
  * @since 17.01.29
  * @author Thibault Meyer
  */
object EbeanMigrationWebPath {

  /**
    * @since 17.01.29
    */
  private val applyMigrationPathRegex =
    s"""/@dbmigration/([a-zA-Z0-9_\\-]+)/apply""".r

  /**
    * @since 17.01.29
    * @author Thibault Meyer
    */
  object migratePath {

    /**
      * Create a migration path
      *
      * @since 17.01.29
      */
    def apply(dbName: String): String = {
      s"/@dbmigration/${dbName}/apply"
    }

    /**
      * Extract variable
      *
      * @since 17.01.29
      * @param path The path
      */
    def unapply(path: String): Option[String] = {
      applyMigrationPathRegex.findFirstMatchIn(path).map(_.group(1))
    }
  }

}
