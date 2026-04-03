package edu.teamcandy.services.showtimes

import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import edu.teamcandy.services.exposed.MovieRepository
import edu.teamcandy.services.exposed.ShowtimeRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ShowtimeServices(
    private val theater: Theater,
    private val repository: ShowtimeRepositoryInterface
) {
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")

    fun showOptions() {
        var input: Int?
        do {
            println("Showtime Menu:")
            println("0. Back")
            println("1. View Showtimes")
            println("2. Add Showtime")
            println("3. Delete Showtime")
            print("Enter option (0-3): ")
            input = readlnOrNull()?.toIntOrNull()

            when (input) {
                1 -> viewShowtimes()
                2 -> addShowtime()
                3 -> deleteShowtime()
                0 -> return
                else -> println("Please enter 0-3.")
            }
        } while (input != 0)
    }

    fun viewShowtimes() {
        val showtimes = ShowtimeRepository.getAllShowtimes()
        if (showtimes.isEmpty()) {
            println("No showtimes scheduled.")
            return
        }

        println("Showtimes:")
        showtimes.forEach {
            println("ID: ${it.id} | ${it.movie.name} | ${it.startTime.format(formatter)} - ${it.endTime.format(formatter)}")
        }
    }

    fun addShowtime() {
        val movies = MovieRepository.getAllMovies()
        if (movies.isEmpty()) {
            println("No movies found. Add a movie first.")
            return
        }

        println("Select a movie:")
        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (${movie.durationMinutes} mins)")
        }

        print("Enter the number: ")
        val choice = readlnOrNull()?.toIntOrNull() ?: -1
        if (choice !in 1..movies.size) {
            println("Invalid selection.")
            return
        }
        val selectedMovie = movies[choice - 1]

        print("Enter start time (yyyy-MM-dd HH:mm): ")
        val startTime = LocalDateTime.parse(readln(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        val showtime = Showtime(movie = selectedMovie, startTime = startTime)
        val result = scheduleShowtime(showtime)
        println(result)
    }

    fun deleteShowtime() {
        val showtimes = ShowtimeRepository.getAllShowtimes()
        if (showtimes.isEmpty()) {
            println("No showtimes to delete.")
            return
        }

        showtimes.forEachIndexed { index, showtime ->
            println("${index + 1}. ${showtime.movie.name} | ${showtime.startTime.format(formatter)}")
        }

        print("Enter the number of the showtime to delete: ")
        val choice = readlnOrNull()?.toIntOrNull() ?: -1
        if (choice !in 1..showtimes.size) {
            println("Invalid selection.")
            return
        }

        val success = repository.deleteShowtime(showtimes[choice - 1].id)
        if (success) println("Showtime deleted.") else println("Delete failed.")
    }

    // Schedules a showtime.
    // Returns error/success string message
    fun scheduleShowtime(showtime: Showtime) : String {
        val overlapExists = checkShowtimeOverlap(showtime)
        if (overlapExists) return "This showtime overlaps with another showtime, please try again."

        val saveShowtimeSuccessful = saveShowtime(showtime)
        return if (!saveShowtimeSuccessful) "Something went wrong with scheduling the showtime. Please try again."
        else "${showtime.movie.name} scheduled successfully: from ${showtime.startTime.format(formatter)} to ${showtime.endTime.format(formatter)}!"
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
        repository.addShowtime(showtimeToSave)
        return true
    }
}