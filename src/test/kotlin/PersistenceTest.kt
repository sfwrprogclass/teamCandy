package edu.teamcandy

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.exposed.init as initDB
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.exposed.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

class PersistenceTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // Delete the test DB if it exists
            val dbFile = File("theater.db")
            if (dbFile.exists()) {
                dbFile.delete()
            }
            initDB()
        }
    }

    @Test
    fun testTicketPersistence() {
        val showtimes = ShowtimeRepository.getAllShowtimes()
        assertFalse(showtimes.isEmpty(), "Showtimes should be seeded")
        
        val showtime = showtimes.first()
        val row = 1
        val seatNum = 1
        
        // Ensure seat is not reserved initially
        assertFalse(showtime.seatingChart[row][seatNum].isReserved, "Seat should be initially free")
        
        // Reserve the seat via repository
        val success = ShowtimeRepository.reserveSeat(showtime.id, row, seatNum)
        assertTrue(success, "Reservation should succeed")
        
        // Fetch showtimes again and check persistence
        val updatedShowtimes = ShowtimeRepository.getAllShowtimes()
        val updatedShowtime = updatedShowtimes.find { it.id == showtime.id }!!
        
        assertTrue(updatedShowtime.seatingChart[row][seatNum].isReserved, "Seat should be reserved in the DB")
        
        // Try to reserve again (should fail)
        val secondAttempt = ShowtimeRepository.reserveSeat(showtime.id, row, seatNum)
        assertFalse(secondAttempt, "Second reservation attempt for same seat should fail")
    }

    @Test
    fun testSeedingFix() {
        transaction {
            // Check that we have movies and they are not empty named
            val movies = MovieTable.selectAll().toList()
            assertFalse(movies.isEmpty(), "Movies should be seeded")
            assertTrue(movies.none { it[MovieTable.name].isBlank() }, "No empty-named movies should exist")
        }
    }
}
