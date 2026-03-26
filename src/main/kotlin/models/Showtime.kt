package edu.teamcandy.models

import java.time.LocalDateTime

data class Showtime(
    // Association movie object
    val movie: Movie,
    // When the trailers/previews begin
    val startTime: LocalDateTime,
    // Theater where the show is playing
    val theaterNumber: Int,
    // Seating chart for this specific showtime
    val seatingChart: List<List<Seat>>,
    // Time needed for trailers, to determine the actual end time
    // when taking the movies duration into consideration
    val paddingMinutes: Int = 15
) {
    // Calculate end time from the movies star time, adding
    // trailer padding and movie duration
    val endTime: LocalDateTime
        get() = startTime.plusMinutes((movie.durationMinutes + paddingMinutes).toLong())

    fun isSeatAvailable(row: Int, number: Int): Boolean {
        return row in seatingChart.indices && number in seatingChart[row].indices && !seatingChart[row][number].isReserved
    }

    fun reserveSeat(row: Int, number: Int): Boolean {
        if (isSeatAvailable(row, number)) {
            seatingChart[row][number].isReserved = true
            return true
        }
        return false
    }
}