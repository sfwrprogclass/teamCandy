package edu.teamcandy

import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import edu.teamcandy.services.exposed.init
import edu.teamcandy.services.exposed.startApiAndDatabase
import edu.teamcandy.services.movies.MovieServices
import edu.teamcandy.services.showtimes.ShowtimeServices
import edu.teamcandy.services.theaters.TheaterServices

fun main() {
    startApiAndDatabase()
    init()
    println("Welcome to Candy Theaters Backend!")
    
    val movieServices = MovieServices()
    val theaterServices = TheaterServices()
    
    println("Welcome to Candy Theaters!")

    var input: Int?
    do {
        println("\nMain Menu:")
        println("0. Exit")
        println("1. Movies")
        println("2. Theaters & Auditoriums")
        println("3. Showtimes (Management)")   
        println("4. Sell Tickets (GUI)")
        print("Enter option (0-4): ")
        input = readlnOrNull()?.toIntOrNull()

        when (input) {
            1 -> movieServices.showOptions()
            2 -> theaterServices.showOptions()
            3 -> {
                println("\nSelect a Theater to manage showtimes for:")
                val theaters = TheaterRepository.getAllTheaters()
                if (theaters.isEmpty()) {
                    println("No theaters available. Please create one first.")
                } else {
                    theaters.forEachIndexed { index, theater -> println("${index + 1}. ${theater.name}") }
                    print("Choice: ")
                    val tChoice = readln().toIntOrNull()?.minus(1)
                    if (tChoice != null && tChoice in theaters.indices) {
                        val selectedTheater = theaters[tChoice]
                        println("\nSelect an Auditorium in ${selectedTheater.name}:")
                        val auds = selectedTheater.auditoriums
                        if (auds.isEmpty()) {
                            println("No auditoriums in this theater.")
                        } else {
                            auds.forEachIndexed { index, aud -> println("${index + 1}. Auditorium ${aud.number}") }
                            print("Choice: ")
                            val aChoice = readln().toIntOrNull()?.minus(1)
                            if (aChoice != null && aChoice in auds.indices) {
                                val selectedAud = auds[aChoice]
                                // Need to load showtimes for this auditorium
                                val allShowtimes = ShowtimeRepository.getAllShowtimes().filter { it.auditoriumId == selectedAud.id }
                                selectedAud.showtimeList.clear()
                                selectedAud.showtimeList.addAll(allShowtimes)

                                val showtimeServices = ShowtimeServices(selectedAud, ShowtimeRepository)
                                showtimeServices.showOptions()
                            }
                        }
                    }
                }
            }
            4 -> {
                println("Launching Ticketing GUI...")
                edu.teamcandy.desktop.startComposeApp()
            }
            0 -> println("Goodbye!")
            else -> println("Please enter 0-4.")
        }
    } while (input != 0)
}