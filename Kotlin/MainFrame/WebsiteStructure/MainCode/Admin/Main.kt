package Mainframe.Maincode.Admin

fun main() {
    startServer()  // from WebServer.kt
    println("Server running at http://localhost:8080")

    println("Welcome to Candy Theaters!")

    while (true) {
        println("\nMain Menu:")
        println("1. Schedule a showtime")
        println("2. Admin Options")
        println("3. Exit")
        print("Choose an option: ")

        when (readln().trim()) {
            "1" -> scheduleShowtimeFlow()
            "2" -> showAdminMenu()
            "3" -> return
            else -> println("Invalid option.")
        }
    }
}
