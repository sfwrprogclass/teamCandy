package edu.teamcandy.models

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowtimeTest {

    private val movie = Movie(name = "Test Movie", durationMinutes = 120)
    private val baseTime = LocalDateTime.of(2026, 3, 1, 10, 0)
    private val emptySeating = List(5) { r -> List(10) { c -> Seat(r, c) } }

    @Test
    fun `endTime adds default padding and movie duration to startTime`() {
        val showtime = Showtime(movie = movie, startTime = baseTime, theaterNumber = 1, seatingChart = emptySeating)

        assertEquals(baseTime.plusMinutes(135), showtime.endTime) // 120 min + 15 default padding
    }

    @Test
    fun `endTime uses custom padding when provided`() {
        val showtime = Showtime(movie = movie, startTime = baseTime, theaterNumber = 1, seatingChart = emptySeating, paddingMinutes = 30)

        assertEquals(baseTime.plusMinutes(150), showtime.endTime) // 120 min + 30 custom padding
    }
}
