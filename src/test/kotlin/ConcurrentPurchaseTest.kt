package edu.teamcandy

import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.exposed.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentPurchaseTest {

    @Test
    fun testConcurrentPurchases() {
        // Use a unique file for this test to avoid conflicts and ensure schema is updated
        val testDbFile = "test_concurrent_${System.currentTimeMillis()}.db"
        Database.connect("jdbc:sqlite:$testDbFile", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable, TicketTable)
        }
        
        try {
            // Ensure we have at least one movie and showtime in the REAL database
            val showtime = transaction {
                println("[DEBUG_LOG] Creating test data")
                val theaterId = TheaterTable.insert {
                    it[name] = "Test Theater"
                    it[location] = "Test Location"
                } get TheaterTable.id

                val audId = AuditoriumTable.insert {
                    it[number] = 1
                    it[this.theaterId] = theaterId
                    it[rows] = 5
                    it[seatsPerRow] = 10
                } get AuditoriumTable.id

                val movieId = MovieTable.insert {
                    it[name] = "Test Movie"
                    it[durationMinutes] = 120
                    it[rating] = "PG"
                    it[description] = "Test Description"
                } get MovieTable.id

                val showtimeId = ShowtimeTable.insert {
                    it[movie] = movieId
                    it[startTime] = LocalDateTime.now().plusDays(1)
                    it[paddingMinutes] = 15
                    it[auditoriumId] = audId
                } get ShowtimeTable.id
                
                ShowtimeRepository.getAllShowtimes().first { it.id == showtimeId }
            }

        println("[DEBUG_LOG] Selected Showtime ID: ${showtime.id}")
        val bookingService = BookingService()

        val numThreads = 10
        val executor = Executors.newFixedThreadPool(numThreads)
        val latch = CountDownLatch(1)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        val row = 0
        val seatNumber = 0

        for (i in 1..numThreads) {
            executor.submit {
                latch.await()
                val result = bookingService.bookTicket(showtime, row, seatNumber)
                if (result.contains("successfully")) {
                    successCount.incrementAndGet()
                } else {
                    failureCount.incrementAndGet()
                }
            }
        }

        latch.countDown()
        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(100)
        }

        println("Success count: ${successCount.get()}")
        println("Failure count: ${failureCount.get()}")

        // Only ONE should succeed
        assertEquals(1, successCount.get(), "Only one thread should have successfully booked the seat")
        assertEquals(numThreads - 1, failureCount.get(), "All other threads should have failed")
    } finally {
        java.io.File(testDbFile).delete()
    }
}
}
