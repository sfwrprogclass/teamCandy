package edu.teamcandy

import edu.teamcandy.models.Theater
import edu.teamcandy.repositories.MovieRepository
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.Scheduler
import edu.teamcandy.services.exposed.MovieDatabase
import edu.teamcandy.utils.Constants
import java.time.LocalDateTime

fun main() {
    MovieDatabase.init()
    println("Welcome to Candy Theaters Backend!")
    
    val theaterOne = Theater(1)
    val scheduler = Scheduler(theaterOne)
    val movieRepository = MovieRepository()
    val bookingService = BookingService()

    while (true) {
        println("\n--- Backend Menu ---")
        println("1. Schedule Showtime")
        println("2. Sell Ticket")
        println("3. View Schedule")
        println("4. Exit")
        print("Select an option: ")

        when (readln().trim()) {
            "1" -> {
                println("\nSelect a movie:")
                val movies = movieRepository.getAllMovies()
                movies.forEachIndexed { index, movie ->
                    println("${index + 1}. ${movie.name}")
                }
                print("Choice: ")
                val movieChoice = readln().toIntOrNull()?.minus(1)
                if (movieChoice != null && movieChoice in movies.indices) {
                    val selectedMovie = movies[movieChoice]
                    print("Enter showtime (yyyy-MM-dd HH:mm): ")
                    val startTimeInput = readln().trim()
                    try {
                        val startTime = LocalDateTime.parse(startTimeInput, Constants.INPUT_FORMATTER)
                        println(scheduler.scheduleShowtime(selectedMovie, startTime))
                    } catch (e: Exception) {
                        println("Invalid date format.")
                    }
                } else {
                    println("Invalid movie selection.")
                }
            }
            "2" -> {
                if (theaterOne.showtimeList.isEmpty()) {
                    println("No showtimes available.")
                } else {
                    println("\nSelect a showtime to sell a ticket for:")
                    theaterOne.showtimeList.sortedBy { it.startTime }.forEachIndexed { index, showtime ->
                        println("${index + 1}. ${showtime.movie.name} at ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)}")
                    }
                    print("Choice: ")
                    val showChoice = readln().toIntOrNull()?.minus(1)
                    val sortedShowtimes = theaterOne.showtimeList.sortedBy { it.startTime }
                    if (showChoice != null && showChoice in sortedShowtimes.indices) {
                        val selectedShowtime = sortedShowtimes[showChoice]
                        
                        // Display seating chart
                        println("\nSeating Chart for Theater ${selectedShowtime.theaterNumber}:")
                        println("  1 2 3 4 5 6 7 8 9 10")
                        selectedShowtime.seatingChart.forEachIndexed { rIndex, row ->
                            print("${'A' + rIndex} ")
                            row.forEach { seat ->
                                print(if (seat.isReserved) "X " else ". ")
                            }
                            println()
                        }

                        print("Enter seat (e.g., A1): ")
                        val seatInput = readln().trim().uppercase()
                        if (seatInput.length >= 2) {
                            val row = seatInput[0] - 'A'
                            val col = seatInput.substring(1).toIntOrNull()?.minus(1)
                            if (col != null && row in selectedShowtime.seatingChart.indices && col in selectedShowtime.seatingChart[0].indices) {
                                println(bookingService.sellTicket(selectedShowtime, row, col))
                            } else {
                                println("Invalid seat.")
                            }
                        } else {
                            println("Invalid seat format.")
                        }
                    } else {
                        println("Invalid showtime selection.")
                    }
                }
            }
            "3" -> {
                if (theaterOne.showtimeList.isEmpty()) {
                    println("No showtimes scheduled for Theater ${theaterOne.number}")
                } else {
                    println("Showtimes for Theater ${theaterOne.number}:")
                    for (showtime in theaterOne.showtimeList.sortedBy { it.startTime }) {
                        println("${showtime.movie.name} - starts at ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)}, ends at ${showtime.endTime.format(Constants.SHOWTIME_FORMATTER)}")
                    }
                }
            }
            "4" -> break
            else -> println("Invalid option.")
        }
    }
    println("Goodbye!")
}
