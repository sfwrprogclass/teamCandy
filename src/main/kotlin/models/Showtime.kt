package edu.teamcandy.models

import java.time.LocalDateTime

data class Showtime(
    // Association movie object
    val movie: Movie,
    // When the trailers/previews begin
    val startTime: LocalDateTime,
    // Time needed for trailers, to determine the actual end time
    // when taking the movies duration into consideration
    val paddingMinutes: Int = 15
) {
    // Calculate end time from the movies star time, adding
    // trailer padding and movie duration
    val endTime: LocalDateTime
        get() = startTime.plusMinutes((movie.durationMinutes + paddingMinutes).toLong())


}