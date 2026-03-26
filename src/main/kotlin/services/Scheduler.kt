package edu.teamcandy.services

import edu.teamcandy.models.Theater
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.utils.Constants
import java.time.LocalDateTime

class Scheduler (private val theater: Theater) {

    // Schedules a showtime.
    // Returns error/success string message
    fun scheduleShowtime(movie: Movie, startTime: LocalDateTime) : String {
        // Generate seating chart for the new showtime
        val seatingChart = List(theater.rows) { row ->
            List(theater.seatsPerRow) { seatNum ->
                Seat(row, seatNum)
            }
        }
        val showtime = Showtime(movie, startTime, theater.number, seatingChart)

        // set a default error message
        val successMessage = "${showtime.movie.name} scheduled successfully: from ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)} to ${
            showtime.endTime.format(Constants.SHOWTIME_FORMATTER)}!"

        // check for overlap
        val overlapExists = checkShowtimeOverlap(showtime)

        if(overlapExists){
            return "This showtime overlapped with another showtime, please try again."
        }

        // save to theater's schedule
        val saveShowtimeSuccessful = saveShowtime(showtime)

        return if(saveShowtimeSuccessful == false) "Something went wrong with scheduling the showtime. Please try again."
        else successMessage
    }

    // Check a showtime overlap for a specific theater
    private fun checkShowtimeOverlap(newShowtime: Showtime) : Boolean {

        for (existingShowtime in theater.showtimeList) {
            val newStartsBeforeExistingEnds = newShowtime.startTime < existingShowtime.endTime
            val newEndsAfterExistingStarts = newShowtime.endTime > existingShowtime.startTime

            if (newStartsBeforeExistingEnds && newEndsAfterExistingStarts) {
                return true
            }
        }

        return false
    }

    private fun saveShowtime(showtimeToSave: Showtime) : Boolean {
        return theater.showtimeList.add(showtimeToSave)
    }
}