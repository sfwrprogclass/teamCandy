package edu.teamcandy

import edu.teamcandy.models.Theater
import edu.teamcandy.models.Showtime
import edu.teamcandy.repositories.MovieRepository
import edu.teamcandy.services.Scheduler
import edu.teamcandy.services.BookingService
import edu.teamcandy.utils.Constants
import java.time.LocalDateTime

class CustomerWebsite(private val theaters: List<Theater>) {
    private val bookingService = BookingService()

    fun displaySchedule() {
        println("\n--- Customer Website: Candy Theaters Schedule ---")
        if (theaters.all { it.showtimeList.isEmpty() }) {
            println("No showtimes available at the moment. Please check back later!")
            return
        }

        for (theater in theaters) {
            if (theater.showtimeList.isNotEmpty()) {
                println("\nTheater ${theater.number}:")
                val sortedShowtimes = theater.showtimeList.sortedBy { it.startTime }
                for (showtime in sortedShowtimes) {
                    println("  ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)} - ${showtime.movie.name} (${showtime.movie.durationMinutes} min)")
                }
            }
        }
        println("--------------------------------------------------\n")
    }

    fun searchAndBook() {
        val movieRepository = MovieRepository()
        println("\n--- Online Ticket Booking ---")
        print("Search for a movie (title, cast, or genre): ")
        val query = readln().trim()
        val foundMovies = movieRepository.searchMovies(query)

        if (foundMovies.isEmpty()) {
            println("No movies found matching '$query'.")
            return
        }

        println("\nMatching Movies:")
        foundMovies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (${movie.durationMinutes} min)")
            println("   Cast: ${movie.cast.joinToString()}")
            println("   Genres: ${movie.genres.joinToString()}")
            println("   Description: ${movie.description}")
        }

        print("\nSelect a movie number to see showtimes: ")
        val movieChoice = readln().toIntOrNull()?.minus(1)
        if (movieChoice == null || movieChoice !in foundMovies.indices) {
            println("Invalid choice.")
            return
        }

        val selectedMovie = foundMovies[movieChoice]
        val allShowtimes = theaters.flatMap { it.showtimeList }.filter { it.movie.name == selectedMovie.name }

        if (allShowtimes.isEmpty()) {
            println("No showtimes available for ${selectedMovie.name}.")
            return
        }

        println("\nAvailable Showtimes for ${selectedMovie.name}:")
        allShowtimes.forEachIndexed { index, showtime ->
            println("${index + 1}. Theater ${showtime.theaterNumber} at ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)}")
        }

        print("\nSelect a showtime number to book: ")
        val showChoice = readln().toIntOrNull()?.minus(1)
        if (showChoice == null || showChoice !in allShowtimes.indices) {
            println("Invalid choice.")
            return
        }

        val selectedShowtime = allShowtimes[showChoice]
        println("\nSeating Chart for Theater ${selectedShowtime.theaterNumber}:")
        println("  1 2 3 4 5 6 7 8 9 10")
        selectedShowtime.seatingChart.forEachIndexed { rIndex, row ->
            print("${'A' + rIndex} ")
            row.forEach { seat ->
                print(if (seat.isReserved) "X " else ". ")
            }
            println()
        }

        print("\nEnter seat (e.g., A1): ")
        val seatInput = readln().trim().uppercase()
        if (seatInput.length < 2) {
            println("Invalid seat format.")
            return
        }

        val row = seatInput[0] - 'A'
        val col = seatInput.substring(1).toIntOrNull()?.minus(1)

        if (col == null || row !in selectedShowtime.seatingChart.indices || col !in selectedShowtime.seatingChart[0].indices) {
            println("Invalid seat.")
            return
        }

        println(bookingService.bookTicket(selectedShowtime, row, col))
    }
}

fun main() {
    // Mocking some data for the website
    val movieRepo = MovieRepository()
    val theater1 = Theater(1)
    val theater2 = Theater(2)
    
    val movies = movieRepo.getAllMovies()
    
    val scheduler1 = Scheduler(theater1)
    val scheduler2 = Scheduler(theater2)
    
    // Add some sample showtimes using scheduler to generate seating charts
    scheduler1.scheduleShowtime(movies[0], LocalDateTime.now().plusHours(2))
    scheduler1.scheduleShowtime(movies[1], LocalDateTime.now().plusHours(5))
    scheduler2.scheduleShowtime(movies[2], LocalDateTime.now().plusHours(3))
    scheduler2.scheduleShowtime(movies[0], LocalDateTime.now().plusHours(8)) // Extra showtime for same movie

    val website = CustomerWebsite(listOf(theater1, theater2))

    while (true) {
        println("\n--- Candy Theaters: Customer Menu ---")
        println("1. View Full Schedule")
        println("2. Search Movies and Book Tickets")
        println("3. Exit")
        print("Select an option: ")

        when (readln().trim()) {
            "1" -> website.displaySchedule()
            "2" -> website.searchAndBook()
            "3" -> {
                println("Thank you for visiting Candy Theaters!")
                break
            }
            else -> println("Invalid option. Please try again.")
        }
    }
}
