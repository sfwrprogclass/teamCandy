package edu.teamcandy.services

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Theater
import edu.teamcandy.utils.Constants
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
        val result = scheduler.scheduleShowtime(movie, baseTime)

        val expected = "Test Movie scheduled successfully: from 03/01/2026 10:00 AM to 03/01/2026 12:15 PM!"
        assertEquals(expected, result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime does not overlap`() {
        scheduler.scheduleShowtime(movie, baseTime)

        val startTime = baseTime.plusHours(3)
        val result = scheduler.scheduleShowtime(movie, startTime)

        val expected = "Test Movie scheduled successfully: from 03/01/2026 01:00 PM to 03/01/2026 03:15 PM!"
        assertEquals(expected, result)
    }

    @Test
    fun `scheduleShowtime returns error when new showtime overlaps an existing one`() {
        scheduler.scheduleShowtime(movie, baseTime)

        val result = scheduler.scheduleShowtime(movie, baseTime.plusHours(1))

        assertEquals("This showtime overlapped with another showtime, please try again.", result)
    }

    @Test
    fun `scheduleShowtime returns success when new showtime starts exactly when existing one ends`() {
        scheduler.scheduleShowtime(movie, baseTime)

        val result = scheduler.scheduleShowtime(movie, baseTime.plusMinutes(135))

        assertEquals("Test Movie scheduled successfully: from 03/01/2026 12:15 PM to 03/01/2026 02:30 PM!", result)
    }
}
