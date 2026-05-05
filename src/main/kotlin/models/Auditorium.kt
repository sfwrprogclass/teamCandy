package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Auditorium(
    // Auditorium ID
    val id: Int = 0,
    // Auditorium number identifier within the theater
    val number: Int,
    // Theater this auditorium belongs to
    val theaterId: Int = 0,
    // Rows and seats per row
    val rows: Int = 5,
    val seatsPerRow: Int = 10,
    // List of show times for this auditorium
    val showtimeList: MutableList<Showtime> = mutableListOf()
)