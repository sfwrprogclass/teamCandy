package edu.teamcandy.services

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Auditorium
import java.time.LocalDateTime

class Scheduler(private val theater: Auditorium) {

    fun scheduleShowtime(movie: Movie, startTime: LocalDateTime): String {
        val seatingChart = List(theater.rows) { row ->
            List(theater.seatsPerRow) { col ->
                Seat(row, col)
            }
        }
        val newShowtime = Showtime(
            movie = movie,
            startTime = startTime,
            auditoriumId = theater.id,
            seatingChart = seatingChart
        )

        val overlap = theater.showtimeList.any { existing ->
            val newEnds = newShowtime.endTime
            val existingEnds = existing.endTime
            (startTime < existingEnds && newEnds > existing.startTime)
        }

        return if (overlap) {
            "Error: Showtime overlaps with an existing one."
        } else {
            theater.showtimeList.add(newShowtime)
            "Successfully scheduled ${movie.name} at ${startTime}."
        }
    }

    fun scheduleShowtime(showtime: Showtime): String {
        val overlap = theater.showtimeList.any { existing ->
            val newEnds = showtime.endTime
            val existingEnds = existing.endTime
            (showtime.startTime < existingEnds && newEnds > existing.startTime)
        }

        return if (overlap) {
            "Error: Showtime overlaps with an existing one."
        } else {
            theater.showtimeList.add(showtime)
            "Successfully scheduled ${showtime.movie.name} at ${showtime.startTime}."
        }
    }
}
