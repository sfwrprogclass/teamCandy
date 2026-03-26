 package edu.teamcandy

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Theater
import edu.teamcandy.models.Showtime
import edu.teamcandy.repositories.MovieRepository
import edu.teamcandy.services.Scheduler
import edu.teamcandy.services.BookingService
import edu.teamcandy.utils.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

fun main() {
    val movieRepository = MovieRepository()
    val theaterOne = Theater(1)
    val scheduler = Scheduler(theaterOne)
    val bookingService = BookingService()

    while (true) {
        println("\nWelcome to Candy Theaters Backend!")
        println("1. Schedule a showtime")
        println("2. Sell a ticket (In-Person)")
        println("3. Exit")
        print("Select an option: ")

        when (readln().trim()) {
            "1" -> {
                val movies = movieRepository.getAllMovies()
                println("\nAvailable Movies:")
                movies.forEachIndexed { index, movie -> println("${index + 1}. ${movie.name} (${movie.durationMinutes} min)") }

                print("Enter movie number or title: ")
                val movieInput = readln().trim()
                val selectedMovie = if (movieInput.toIntOrNull() != null) {
                    val index = movieInput.toInt() - 1
                    if (index in movies.indices) movies[index] else null
                } else {
                    movieRepository.findMovieByName(movieInput)
                }

                if (selectedMovie == null) {
                    println("Movie not found.")
                    continue
                }

                print("Enter showtime (yyyy-MM-dd HH:mm): ")
                val startTimeInput = readln()
                val startTime = try {
                    LocalDateTime.parse(startTimeInput, Constants.INPUT_FORMATTER)
                } catch (e: DateTimeParseException) {
                    println("Invalid date format.")
                    continue
                }

                println(scheduler.scheduleShowtime(selectedMovie, startTime))
            }
            "2" -> {
                if (theaterOne.showtimeList.isEmpty()) {
                    println("No showtimes scheduled.")
                    continue
                }
                println("\nSelect a showtime:")
                theaterOne.showtimeList.forEachIndexed { index, showtime ->
                    println("${index + 1}. ${showtime.movie.name} at ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)}")
                }
                val choice = readln().toIntOrNull()?.minus(1)
                if (choice == null || choice !in theaterOne.showtimeList.indices) {
                    println("Invalid choice.")
                    continue
                }
                val selectedShowtime = theaterOne.showtimeList[choice]
                
                println("\nSeating Chart:")
                selectedShowtime.seatingChart.forEachIndexed { rIndex, row ->
                    print("${'A' + rIndex} ")
                    row.forEach { seat -> print(if (seat.isReserved) "X " else ". ") }
                    println()
                }
                print("Enter seat (e.g., A1): ")
                val seatInput = readln().trim().uppercase()
                if (seatInput.length >= 2) {
                    val r = seatInput[0] - 'A'
                    val c = seatInput.substring(1).toIntOrNull()?.minus(1)
                    if (c != null && r in selectedShowtime.seatingChart.indices && c in selectedShowtime.seatingChart[0].indices) {
                        println(bookingService.sellTicket(selectedShowtime, r, c))
                    } else println("Invalid seat.")
                } else println("Invalid seat.")
            }
            "3" -> break
            else -> println("Invalid option.")
        }
    }
}

