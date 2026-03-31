package edu.teamcandy.exposed

import org.jetbrains.exposed.sql.*

object MovieTable : Table("movies") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val durationMinutes = integer("duration_minutes")
    val rating = varchar("rating", 10)
    val description = text("description")

    override val primaryKey = PrimaryKey(id)
}
