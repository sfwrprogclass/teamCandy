package edu.teamcandy.services

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Auditorium
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class SchedulerTest {

    private val movie = Movie(name = "Test Movie", durationMinutes = 120)
    private val theater = Auditorium(id = 1, number = 1)
    private val scheduler = Scheduler(theater)
    private val baseTime = LocalDateTime.of(2026, 3, 1, 10, 0)

    private fun createShowtime(startTime: LocalDateTime): Showtime {
        val seatingChart = List(theater.rows) { r -> List(theater.seatsPerRow) { c -> Seat(r, c) } }
        return Showtime(movie = movie, startTime = startTime, auditoriumId = theater.id, seatingChart = seatingChart)
    }

    @Test
    fun `scheduleShowtime returns success message when theater is empty`() {
        val result = scheduler.scheduleShowtime(movie, baseTime)

        val expected = "Successfully scheduled Test Movie at 2026-03-01T10:00."
        assertEquals(expected, result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime does not overlap`() {
        scheduler.scheduleShowtime(movie, baseTime)

        val startTime = baseTime.plusHours(3)
        val result = scheduler.scheduleShowtime(movie, startTime)

        val expected = "Successfully scheduled Test Movie at 2026-03-01T13:00."
        assertEquals(expected, result)
    }

    @Test
    fun `scheduleShowtime returns error when new showtime overlaps an existing one`() {
        scheduler.scheduleShowtime(movie, baseTime)

        val result = scheduler.scheduleShowtime(movie, baseTime.plusHours(1))

        assertEquals("Error: Showtime overlaps with an existing one.", result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime starts exactly when existing one ends`() {
        scheduler.scheduleShowtime(movie, baseTime)

        // 120 min duration + 15 min buffer = 135 minutes
        val startTime = baseTime.plusMinutes(135)
        val result = scheduler.scheduleShowtime(movie, startTime)

        val expected = "Successfully scheduled Test Movie at 2026-03-01T12:15."
        assertEquals(expected, result)
    }
}
