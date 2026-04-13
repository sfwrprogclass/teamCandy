package Mainframe.Maincode.Admin

import org.jetbrains.exposed.sql.Table

object MoviesTable : Table("movies") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val duration = integer("duration")
    val description = text("description")
    val theater = varchar("theater", 255)
    val poster = varchar("poster", 255)

    override val primaryKey = PrimaryKey(id)
}
