package edu.teamcandy.models

data class Theater(
    // Theater number identifier
    val number: Int,
    // List of show times for this theater
    val showtimeList: MutableList<Showtime> = mutableListOf()
)