package edu.teamcandy.exposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object MovieTable : Table("movies") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val durationMinutes = integer("duration_minutes")
    val rating = varchar("rating", 10)
    val description = text("description")

    override val primaryKey = PrimaryKey(id)
}

object ShowtimeTable : Table("showtimes") {
    val id = integer("id").autoIncrement()
    val movie = integer("movie_id").references(MovieTable.id)
    val startTime = datetime("start_time").default(LocalDateTime.now())
    val paddingMinutes = integer("padding_minutes").default(15)

    override val primaryKey = PrimaryKey(id)
}
