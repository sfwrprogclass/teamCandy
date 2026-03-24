package edu.teamcandy.services

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class SchedulerTest {

    private val movie = Movie(name = "Test Movie", durationMinutes = 120)
    private val theater = Theater(number = 1)
    private val scheduler = Scheduler(theater)
    private val baseTime = LocalDateTime.of(2026, 3, 1, 10, 0)

    @Test
    fun `scheduleShowtime returns success message when theater is empty`() {
        val showtime = Showtime(movie = movie, startTime = baseTime)

        val result = scheduler.scheduleShowtime(showtime)

        assertEquals("Test Movie scheduled successfully: from 03/01/2026 10:00 AM to 03/01/2026 12:15 PM!", result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime does not overlap`() {
        val firstShowtime = Showtime(movie = movie, startTime = baseTime)
        scheduler.scheduleShowtime(firstShowtime)

        val secondShowtime = Showtime(movie = movie, startTime = baseTime.plusHours(3))

        val result = scheduler.scheduleShowtime(secondShowtime)

        assertEquals("Test Movie scheduled successfully: from 03/01/2026 01:00 PM to 03/01/2026 03:15 PM!", result)
    }

    @Test
    fun `scheduleShowtime returns error when new showtime overlaps an existing one`() {
        val firstShowtime = Showtime(movie = movie, startTime = baseTime)
        scheduler.scheduleShowtime(firstShowtime)

        val overlappingShowtime = Showtime(movie = movie, startTime = baseTime.plusHours(1))

        val result = scheduler.scheduleShowtime(overlappingShowtime)

        assertEquals("This showtime overlapped with another showtime, please try again.", result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime starts exactly when existing one ends`() {
        val firstShowtime = Showtime(movie = movie, startTime = baseTime)
        scheduler.scheduleShowtime(firstShowtime)

        val adjacentShowtime = Showtime(movie = movie, startTime = baseTime.plusMinutes(135))

        val result = scheduler.scheduleShowtime(adjacentShowtime)

        assertEquals("Test Movie scheduled successfully: from 03/01/2026 12:15 PM to 03/01/2026 02:30 PM!", result)
    }
}
