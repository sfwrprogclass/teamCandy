package edu.teamcandy.models

import java.time.LocalDateTime

data class Showtime(
    val id: Int = 0,
    val movie: Movie,
    val startTime: LocalDateTime,
    val paddingMinutes: Long = 15
) {
    val endTime: LocalDateTime
        get() = startTime.plusMinutes(movie.durationMinutes.toLong() + paddingMinutes)
}
