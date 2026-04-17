package edu.teamcandy.services.theaters

import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Theater
import edu.teamcandy.repository.TheaterRepositoryInterface
import edu.teamcandy.services.exposed.TheaterRepository

class TheaterServices(private val repository: TheaterRepositoryInterface = TheaterRepository) {
    fun showOptions() {
        var input: Int?
        do {
            println("\n--- Theater Management ---")
            println("0. Back")
            println("1. View Theaters")
            println("2. Add Theater")
            println("3. Manage Auditoriums")
            println("4. Delete Theater")
            print("Enter option (0-4): ")
            input = readlnOrNull()?.toIntOrNull()

            when (input) {
                1 -> viewTheaters()
                2 -> addTheater()
                3 -> manageAuditoriumsMenu()
                4 -> deleteTheater()
                0 -> println("Returning to main menu...")
                else -> println("Invalid option.")
            }
        } while (input != 0)
    }

    private fun viewTheaters() {
        val theaters = repository.getAllTheaters()
        if (theaters.isEmpty()) {
            println("No theaters found.")
            return
        }
        println("\nTheaters:")
        theaters.forEach { theater ->
            println("ID: ${theater.id} | Name: ${theater.name} | Location: ${theater.location} | Auditoriums: ${theater.auditoriums.size}")
        }
    }

    private fun addTheater() {
        print("Enter theater name: ")
        val name = readln()
        print("Enter theater location: ")
        val location = readln()
        val theater = Theater(name = name, location = location)
        val id = repository.addTheater(theater)
        println("Theater added with ID: $id")
    }

    private fun deleteTheater() {
        val theaters = repository.getAllTheaters()
        if (theaters.isEmpty()) return
        theaters.forEachIndexed { index, theater -> println("${index + 1}. ${theater.name}") }
        print("Select theater to delete: ")
        val choice = readln().toIntOrNull()?.minus(1)
        if (choice != null && choice in theaters.indices) {
            val success = repository.deleteTheater(theaters[choice].id)
            if (success) println("Theater deleted.") else println("Delete failed.")
        }
    }

    private fun manageAuditoriumsMenu() {
        val theaters = repository.getAllTheaters()
        if (theaters.isEmpty()) {
            println("Please add a theater first.")
            return
        }
        println("\nSelect a theater to manage auditoriums:")
        theaters.forEachIndexed { index, theater -> println("${index + 1}. ${theater.name}") }
        print("Choice: ")
        val choice = readln().toIntOrNull()?.minus(1)
        if (choice == null || choice !in theaters.indices) return
        
        val theater = theaters[choice]
        var input: Int?
        do {
            println("\n--- Managing Auditoriums for ${theater.name} ---")
            println("0. Back")
            println("1. View Auditoriums")
            println("2. Add Auditorium")
            println("3. Delete Auditorium")
            print("Choice: ")
            input = readlnOrNull()?.toIntOrNull()

            when (input) {
                1 -> {
                    val auds = repository.getAuditoriumsByTheater(theater.id)
                    if (auds.isEmpty()) println("No auditoriums.")
                    auds.forEach { println("ID: ${it.id} | Number: ${it.number} | Rows: ${it.rows} | Seats/Row: ${it.seatsPerRow}") }
                }
                2 -> {
                    print("Auditorium number: ")
                    val num = readln().toIntOrNull() ?: 0
                    print("Rows: ")
                    val rows = readln().toIntOrNull() ?: 5
                    print("Seats per row: ")
                    val seats = readln().toIntOrNull() ?: 10
                    repository.addAuditorium(Auditorium(number = num, theaterId = theater.id, rows = rows, seatsPerRow = seats))
                    println("Auditorium added.")
                }
                3 -> {
                    val auds = repository.getAuditoriumsByTheater(theater.id)
                    auds.forEachIndexed { i, a -> println("${i + 1}. Auditorium ${a.number}") }
                    print("Select to delete: ")
                    val c = readln().toIntOrNull()?.minus(1)
                    if (c != null && c in auds.indices) {
                        repository.deleteAuditorium(auds[c].id)
                        println("Deleted.")
                    }
                }
            }
        } while (input != 0)
    }
}
