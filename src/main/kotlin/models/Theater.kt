package edu.teamcandy.models

data class Theater(
    // Theater number identifier
    val number: Int,
    // Rows and seats per row
    val rows: Int = 5,
    val seatsPerRow: Int = 10,
    // List of show times for this theater
    val showtimeList: MutableList<Showtime> = mutableListOf()
)