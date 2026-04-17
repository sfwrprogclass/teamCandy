package edu.teamcandy

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.BookingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentPurchaseTest {

    @Test
    fun testConcurrentPurchases() {
        val movie = Movie(1, "Test Movie", 120, "PG", "Test Description")
        val seatingChart = List(5) { r -> List(10) { c -> Seat(r, c) } }
        val showtime = Showtime(1, movie, LocalDateTime.now(), 1, seatingChart)
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
    }
}
