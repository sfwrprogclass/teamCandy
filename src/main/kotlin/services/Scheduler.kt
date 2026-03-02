package edu.teamcandy.services

import edu.teamcandy.models.Theater
import edu.teamcandy.models.Showtime
import java.time.format.DateTimeFormatter

class Scheduler (private val theater: Theater) {

    // Schedules a showtime.
    // Returns error/success string message
    fun scheduleShowtime(showtime: Showtime) : String {
        // Time formatter for listing showtimes
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")

        // set a default error message
        val successMessage = "${showtime.movie.name} scheduled successfully: from ${showtime.startTime.format(formatter)} to ${
            showtime.endTime.format(formatter)}!"

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
        theater.showtimeList.add(showtimeToSave)

        // TODO: add checks showtime was saved successfully, return false if it wasn't

        return true
    }
}