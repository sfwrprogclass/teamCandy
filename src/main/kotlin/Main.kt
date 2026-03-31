package edu.teamcandy

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Theater
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.Scheduler
import edu.teamcandy.services.exposed.startApiAndDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    startApiAndDatabase()
    println("Welcome to Candy Theaters!")
    println("")

    // Instantiate theater and scheduler
    val theaterOne = Theater(1)
    val scheduler = Scheduler(theaterOne)

    // TODO: Move to somewhere we can use this across different files for displaying
    //  dates and only have to define it once
    // Time formatter for listing showtimes
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")

    // TODO: Add handling for invalid inputs
    print("Would you like to schedule a showtime? (Y or N): ")
    var scheduleShowtime = readln().trim().lowercase() == "y"

    while (scheduleShowtime) {
        // Get the movie info
        print("Enter a movie title: ")
        val movieName = readln()

        print("Enter the movie duration in minutes: ")
        val movieDuration = readln().toInt()

        print("Enter showtime, time should be in 24 hour format (yyyy-MM-dd HH:mm): ")
        val startTimeInput = readln()
        val startTime = LocalDateTime.parse(startTimeInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

//        val scheduleMessage = scheduler.scheduleShowtime(Showtime(Movie(movieName, movieDuration), startTime))

        // Display success or error message after scheduling showtime
//        println(scheduleMessage)
        println("")

        print("Would you like to schedule another showtime? (Y or N): ")
        scheduleShowtime = readln().trim().lowercase() == "y"
    }

    println("Thank you!")
    println("")


    if (theaterOne.showtimeList.isEmpty()) {
        println("No showtimes scheduled for Theater ${theaterOne.number}")
        return
    }
    else{
        println("Showtimes for Theater ${theaterOne.number}:")
        for (showtime in theaterOne.showtimeList) {
            println("${showtime.movie.name} - starts at ${showtime.startTime.format(formatter)}, ends at ${showtime.endTime.format(formatter)}")
        }
    }
}
