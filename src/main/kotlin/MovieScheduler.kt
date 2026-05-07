import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Data class representing a movie
data class Movie(val title: String)

// Data class representing a showtime (movie + start time)
data class Showtime(val movie: Movie, val startTime: LocalDateTime)

// Class that manages the theater schedule
class TheaterSchedule(private val showtimes: List<Showtime>) {

    // Returns all showtimes sorted by start time
    fun getAllShowtimes() =
        showtimes.sortedBy { it.startTime }

    // Returns showtimes filtered by a specific date
    fun getShowtimesByDate(date: LocalDate) =
        showtimes.filter { it.startTime.toLocalDate() == date }
            .sortedBy { it.startTime }

    // Returns showtimes filtered by movie title (case-insensitive)
    fun getShowtimesByMovie(title: String) =
        showtimes.filter { it.movie.title.equals(title, ignoreCase = true) }
            .sortedBy { it.startTime }

    // Prints a list of showtimes in a readable format
    fun printSchedule(showtimes: List<Showtime>) {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")

        // Check if list is empty
        if (showtimes.isEmpty()) {
            println("No showtimes available.")
            return
        }

        // Loop through each showtime and print it
        showtimes.forEach {
            println("${it.movie.title} - ${it.startTime.format(formatter)}")
        }
    }
}

// Main function (program entry point)
fun main() {

    // Create movie objects
    val lockstockandtwosmokingbarrels = Movie("Lock Stock and Two Smoking Barrels")
    val thewolfman = Movie("The Wolfman")
    val spiderman2 = Movie("Spider-Man 2")
    val inception = Movie("Inception")
    val fightClub = Movie("Fight Club")

    // Create a list of showtimes
    val showtimes = listOf(
        Showtime(lockstockandtwosmokingbarrels, LocalDateTime.of(2026, 3, 25, 18, 0)),
        Showtime(thewolfman, LocalDateTime.of(2026, 3, 25, 20, 30)),
        Showtime(spiderman2, LocalDateTime.of(2026, 3, 25, 22, 0)),
        Showtime(inception, LocalDateTime.of(2026, 3, 26, 14, 0)),
        Showtime(lockstockandtwosmokingbarrels, LocalDateTime.of(2026, 3, 26, 16, 30)),
        Showtime(fightClub, LocalDateTime.of(2026, 3, 26, 19, 0))
    )

    // Create schedule manager
    val schedule = TheaterSchedule(showtimes)

    // Formatter for parsing user date input
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Infinite loop to keep menu running until user exits
    while (true) {

        // Display menu options
        println("\n=== Movie Theater Menu ===")
        println("1. View all showtimes")
        println("2. Filter by date")
        println("3. Filter by movie")
        println("4. Exit")
        print("Enter your choice: ")

        // Read user choice
        when (readLine()) {

            // Option 1: Show all showtimes
            "1" -> {
                println("\nAll Showtimes:")
                schedule.printSchedule(schedule.getAllShowtimes())
            }

            // Option 2: Filter by date
            "2" -> {
                print("Enter date (yyyy-MM-dd): ")
                val input = readLine()

                try {
                    // Convert input string to LocalDate
                    val date = LocalDate.parse(input, dateFormatter)
                    println("\nShowtimes on $date:")
                    schedule.printSchedule(schedule.getShowtimesByDate(date))
                } catch (e: Exception) {
                    println("Invalid date format.")
                }
            }

            // Option 3: Filter by movie name
            "3" -> {
                print("Enter movie name: ")
                val input = readLine()

                if (!input.isNullOrBlank()) {
                    println("\nShowtimes for \"$input\":")
                    schedule.printSchedule(schedule.getShowtimesByMovie(input))
                } else {
                    println("Invalid input.")
                }
            }

            // Option 4: Exit program
            "4" -> {
                println("Goodbye!")
                return
            }

            // Handle invalid menu input
            else -> println("Invalid choice. Please try again.")
        }
    }
}

