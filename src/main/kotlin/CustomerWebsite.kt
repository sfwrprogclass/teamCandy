package edu.teamcandy

import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Theater
import edu.teamcandy.repositories.MovieRepository
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.Scheduler
import edu.teamcandy.utils.Constants
import java.time.LocalDateTime

class CustomerWebsite(private val theaters: List<Theater>) {
    private val bookingService = BookingService()

    fun displaySchedule() {
        println("\n--- Customer Website: Candy Theaters Schedule ---")
        val allAuds = theaters.flatMap { it.auditoriums }
        if (allAuds.all { it.showtimeList.isEmpty() }) {
            println("No showtimes available at the moment. Please check back later!")
            return
        }

        for (theater in theaters) {
            println("\nLocation: ${theater.name} (${theater.location})")
            for (aud in theater.auditoriums) {
                if (aud.showtimeList.isNotEmpty()) {
                    println("  Auditorium ${aud.number}:")
                    val sortedShowtimes = aud.showtimeList.sortedBy { it.startTime }
                    for (showtime in sortedShowtimes) {
                        println("    ${showtime.startTime.format(Constants.SHOWTIME_FORMATTER)} - ${showtime.movie.name} (${showtime.movie.durationMinutes} min)")
                    }
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
        
        data class ShowtimeWithLocation(val theater: Theater, val auditorium: Auditorium, val showtime: edu.teamcandy.models.Showtime)
        val allShowtimes = theaters.flatMap { t -> 
            t.auditoriums.flatMap { a -> 
                a.showtimeList.filter { it.movie.name == selectedMovie.name }
                    .map { ShowtimeWithLocation(t, a, it) }
            }
        }

        if (allShowtimes.isEmpty()) {
            println("No showtimes available for ${selectedMovie.name}.")
            return
        }

        println("\nAvailable Showtimes for ${selectedMovie.name}:")
        allShowtimes.forEachIndexed { index, item ->
            println("${index + 1}. ${item.theater.name} - Auditorium ${item.auditorium.number} at ${item.showtime.startTime.format(Constants.SHOWTIME_FORMATTER)}")
        }

        print("\nSelect a showtime number to book: ")
        val showChoice = readln().toIntOrNull()?.minus(1)
        if (showChoice == null || showChoice !in allShowtimes.indices) {
            println("Invalid choice.")
            return
        }

        val selectedItem = allShowtimes[showChoice]
        val selectedShowtime = selectedItem.showtime
        println("\nSeating Chart for ${selectedItem.theater.name} Auditorium ${selectedItem.auditorium.number}:")
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
    val t1 = Theater(id = 1, name = "Downtown Cinema", location = "123 Main St")
    val a1 = Auditorium(id = 1, number = 1, theaterId = 1)
    val a2 = Auditorium(id = 2, number = 2, theaterId = 1)
    t1.auditoriums.add(a1)
    t1.auditoriums.add(a2)
    
    val movies = movieRepo.getAllMovies()
    
    val scheduler1 = Scheduler(a1)
    val scheduler2 = Scheduler(a2)
    
    // Add some sample showtimes using scheduler to generate seating charts
    if (movies.isNotEmpty()) {
        scheduler1.scheduleShowtime(movies[0], LocalDateTime.now().plusHours(2))
        if (movies.size > 1) scheduler1.scheduleShowtime(movies[1], LocalDateTime.now().plusHours(5))
        if (movies.size > 2) scheduler2.scheduleShowtime(movies[2], LocalDateTime.now().plusHours(3))
        scheduler2.scheduleShowtime(movies[0], LocalDateTime.now().plusHours(8))
    }

    val website = CustomerWebsite(listOf(t1))

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
