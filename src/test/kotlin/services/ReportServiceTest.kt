package edu.teamcandy.services

import edu.teamcandy.exposed.*
import edu.teamcandy.models.*
import edu.teamcandy.services.exposed.MovieRepository
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.test.*

class ReportServiceTest {

    private val reportService = ReportService()

    companion object {
        init {
            Database.connect("jdbc:sqlite:./test_reports.db", "org.sqlite.JDBC")
        }
    }

    @BeforeTest
    fun setUp() {
        transaction {
            SchemaUtils.create(MovieTable, TheaterTable, AuditoriumTable, ShowtimeTable, TicketTable)
        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(TicketTable, ShowtimeTable, AuditoriumTable, TheaterTable, MovieTable)
        }
    }

    @Test
    fun `generateTicketSalesReport - calculates correct totals and movie sales`() {
        // Setup data
        val movie1Id = MovieRepository.addMovie(Movie(name = "Movie 1", durationMinutes = 100, rating = "PG"))
        val movie2Id = MovieRepository.addMovie(Movie(name = "Movie 2", durationMinutes = 120, rating = "R"))
        
        val theaterId = TheaterRepository.addTheater(Theater(name = "Main Street Cinema", location = "Downtown"))
        val audId = TheaterRepository.addAuditorium(Auditorium(number = 1, theaterId = theaterId))

        val startTime1 = LocalDateTime.of(2026, 5, 1, 10, 0)
        val startTime2 = LocalDateTime.of(2026, 5, 1, 14, 0)

        val s1Id = ShowtimeRepository.addShowtime(
            Showtime(
                movie = Movie(id = movie1Id, name = "Movie 1", durationMinutes = 100, rating = "PG"),
                startTime = startTime1,
                auditoriumId = audId,
                seatingChart = emptyList(),
                unitPrice = 10.0
            )
        )

        val s2Id = ShowtimeRepository.addShowtime(
            Showtime(
                movie = Movie(id = movie2Id, name = "Movie 2", durationMinutes = 120, rating = "R"),
                startTime = startTime2,
                auditoriumId = audId,
                seatingChart = emptyList(),
                unitPrice = 15.0
            )
        )

        transaction {
            // Insert tickets
            TicketTable.insert {
                it[showtimeId] = s1Id
                it[row] = 1
                it[seatNumber] = 1
                it[soldAt] = LocalDateTime.of(2026, 5, 1, 9, 0)
            }
            TicketTable.insert {
                it[showtimeId] = s1Id
                it[row] = 1
                it[seatNumber] = 2
                it[soldAt] = LocalDateTime.of(2026, 5, 1, 9, 30)
            }
            TicketTable.insert {
                it[showtimeId] = s2Id
                it[row] = 2
                it[seatNumber] = 1
                it[soldAt] = LocalDateTime.of(2026, 5, 1, 13, 0)
            }
        }

        val startDate = LocalDateTime.of(2026, 5, 1, 0, 0)
        val endDate = LocalDateTime.of(2026, 5, 2, 0, 0)

        val report = reportService.generateTicketSalesReport(theaterId, startDate, endDate)

        assertEquals("Main Street Cinema", report.theaterName)
        assertEquals(3, report.totalTicketsSold)
        assertEquals(35.0, report.totalRevenue)
        assertEquals(2, report.movieSales.size)

        val movie1Sales = report.movieSales.first { it.movieName == "Movie 1" }
        assertEquals(2, movie1Sales.ticketsSold)
        assertEquals(20.0, movie1Sales.revenue)

        val movie2Sales = report.movieSales.first { it.movieName == "Movie 2" }
        assertEquals(1, movie2Sales.ticketsSold)
        assertEquals(15.0, movie2Sales.revenue)
    }

    @Test
    fun `generateTicketSalesReport - filters by date range correctly`() {
        val movie1Id = MovieRepository.addMovie(Movie(name = "Movie 1", durationMinutes = 100, rating = "PG"))
        val theaterId = TheaterRepository.addTheater(Theater(name = "Main Street Cinema", location = "Downtown"))
        val audId = TheaterRepository.addAuditorium(Auditorium(number = 1, theaterId = theaterId))

        val sId = ShowtimeRepository.addShowtime(
            Showtime(
                movie = Movie(id = movie1Id, name = "Movie 1", durationMinutes = 100, rating = "PG"),
                startTime = LocalDateTime.of(2026, 5, 1, 10, 0),
                auditoriumId = audId,
                seatingChart = emptyList(),
                unitPrice = 10.0
            )
        )

        transaction {
            TicketTable.insert {
                it[showtimeId] = sId
                it[row] = 1
                it[seatNumber] = 1
                it[soldAt] = LocalDateTime.of(2026, 4, 30, 23, 59) // Before range
            }
            TicketTable.insert {
                it[showtimeId] = sId
                it[row] = 1
                it[seatNumber] = 2
                it[soldAt] = LocalDateTime.of(2026, 5, 1, 12, 0) // Within range
            }
            TicketTable.insert {
                it[showtimeId] = sId
                it[row] = 1
                it[seatNumber] = 3
                it[soldAt] = LocalDateTime.of(2026, 5, 2, 0, 1) // After range
            }
        }

        val startDate = LocalDateTime.of(2026, 5, 1, 0, 0)
        val endDate = LocalDateTime.of(2026, 5, 1, 23, 59, 59)

        val report = reportService.generateTicketSalesReport(theaterId, startDate, endDate)

        assertEquals(1, report.totalTicketsSold)
        assertEquals(10.0, report.totalRevenue)
    }
}
