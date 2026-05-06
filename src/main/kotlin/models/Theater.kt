package edu.teamcandy.models

data class Theater(
    val number: Int,
    val showtimes: MutableList<Showtime> = mutableListOf()
)
