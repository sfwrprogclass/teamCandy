package edu.teamcandy.services.showtimes

import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import java.time.format.DateTimeFormatter

class ShowtimeServices(private val theater: Theater) {

    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")

    fun scheduleShowtime(showtime: Showtime): String {

        // Check overlap
        for (existing in theater.showtimes) {
            val startsBeforeEnd = showtime.startTime < existing.endTime
            val endsAfterStart = showtime.endTime > existing.startTime

            if (startsBeforeEnd && endsAfterStart) {
                return "This showtime overlapped with another showtime, please try again."
            }
        }

        // Add showtime
        theater.showtimes.add(showtime)

        val start = showtime.startTime.format(formatter)
        val end = showtime.endTime.format(formatter)

        return "${showtime.movie.name} scheduled successfully: from $start to $end!"
    }
}
