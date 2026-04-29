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

object TheaterTable : Table("theaters") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val location = varchar("location", 255).default("")

    override val primaryKey = PrimaryKey(id)
}

object AuditoriumTable : Table("auditoriums") {
    val id = integer("id").autoIncrement()
    val number = integer("number")
    val theaterId = integer("theater_id").references(TheaterTable.id)
    val rows = integer("rows").default(5)
    val seatsPerRow = integer("seats_per_row").default(10)

    override val primaryKey = PrimaryKey(id)
}

object ShowtimeTable : Table("showtimes") {
    val id = integer("id").autoIncrement()
    val movie = integer("movie_id").references(MovieTable.id)
    val startTime = datetime("start_time").default(LocalDateTime.now())
    val paddingMinutes = integer("padding_minutes").default(15)
    val auditoriumId = integer("auditorium_id").references(AuditoriumTable.id)

    override val primaryKey = PrimaryKey(id)
}

object TicketTable : Table("tickets") {
    val id = integer("id").autoIncrement()
    val showtimeId = integer("showtime_id").references(ShowtimeTable.id)
    val row = integer("row")
    val seatNumber = integer("seat_number")
    val soldAt = datetime("sold_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
