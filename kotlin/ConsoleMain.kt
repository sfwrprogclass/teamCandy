package edu.teamcandy

import edu.teamcandy.models.Theater
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.startApiAndDatabase
import edu.teamcandy.services.movies.MovieServices
import edu.teamcandy.services.showtimes.ShowtimeServices

fun main() {
    startApiAndDatabase()

    val theaterOne = Theater(1)
    val movieServices = MovieServices()
    val showtimeServices = ShowtimeServices(theaterOne, ShowtimeRepository)

    println("Welcome to Candy Theaters!")

    var input: Int?
    do {
        println("\nMain Menu:")
        println("0. Exit")
        println("1. Movies")
        println("2. Showtimes")
        print("Enter option (0-2): ")
        input = readlnOrNull()?.toIntOrNull()

        when (input) {
            1 -> movieServices.showOptions()
            2 -> showtimeServices.showOptions()
            0 -> println("Goodbye!")
            else -> println("Please enter 0-2.")
        }
    } while (input != 0)
}