package edu.teamcandy.services

import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.exposed.TheaterTable
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import edu.teamcandy.services.exposed.MovieRepository
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositoryIntegrationTest {

    companion object {
        init {
            Database.connect("jdbc:sqlite:./test_gui.db", "org.sqlite.JDBC")
        }
    }

    @BeforeTest
    fun setUp() {
        transaction {
            SchemaUtils.create(MovieTable, TheaterTable, AuditoriumTable, ShowtimeTable)
        }
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(ShowtimeTable, AuditoriumTable, TheaterTable, MovieTable)
        }
    }

    // --- Movies ---

    @Test
    fun `GUI add movie - persists to database and is retrievable`() {
        MovieRepository.addMovie(Movie(name = "Inception", durationMinutes = 148, rating = "PG-13"))

        val movies = MovieRepository.getAllMovies()

        assertEquals(1, movies.size)
        assertEquals("Inception", movies[0].name)
        assertEquals(148, movies[0].durationMinutes)
        assertEquals("PG-13", movies[0].rating)
    }

    @Test
    fun `GUI delete movie - removes it from the database`() {
        MovieRepository.addMovie(Movie(name = "To Delete", durationMinutes = 90))
        val id = MovieRepository.getAllMovies().first().id

        assertTrue(MovieRepository.deleteMovie(id))
        assertTrue(MovieRepository.getAllMovies().isEmpty())
    }

    @Test
    fun `GUI edit movie - updates name, duration, and rating`() {
        MovieRepository.addMovie(Movie(name = "Old Title", durationMinutes = 90))
        val id = MovieRepository.getAllMovies().first().id

        MovieRepository.updateMovie(id, Movie(name = "New Title", durationMinutes = 110, rating = "R"))

        val updated = MovieRepository.getAllMovies().first()
        assertEquals("New Title", updated.name)
        assertEquals(110, updated.durationMinutes)
        assertEquals("R", updated.rating)
    }

    // --- Theaters & Auditoriums ---

    @Test
    fun `GUI add theater - persists and is retrievable`() {
        TheaterRepository.addTheater(Theater(name = "Galaxy Cinema", location = "Uptown"))

        val theaters = TheaterRepository.getAllTheaters()

        assertEquals(1, theaters.size)
        assertEquals("Galaxy Cinema", theaters[0].name)
        assertEquals("Uptown", theaters[0].location)
    }

    @Test
    fun `GUI add auditorium - links to theater and is retrievable`() {
        val theaterId = TheaterRepository.addTheater(Theater(name = "Galaxy Cinema", location = "Uptown"))

        TheaterRepository.addAuditorium(Auditorium(number = 2, theaterId = theaterId, rows = 8, seatsPerRow = 12))

        val auditoriums = TheaterRepository.getAuditoriumsByTheater(theaterId)
        assertEquals(1, auditoriums.size)
        assertEquals(2, auditoriums[0].number)
        assertEquals(8, auditoriums[0].rows)
        assertEquals(12, auditoriums[0].seatsPerRow)
    }

    @Test
    fun `GUI delete auditorium - removes it from the theater`() {
        val theaterId = TheaterRepository.addTheater(Theater(name = "Galaxy Cinema", location = "Uptown"))
        val audId = TheaterRepository.addAuditorium(Auditorium(number = 1, theaterId = theaterId))

        assertTrue(TheaterRepository.deleteAuditorium(audId))
        assertTrue(TheaterRepository.getAuditoriumsByTheater(theaterId).isEmpty())
    }

    // --- Showtimes ---

    @Test
    fun `GUI add showtime - persists and is retrievable with correct movie and time`() {
        MovieRepository.addMovie(Movie(name = "Dune", durationMinutes = 155))
        val movie = MovieRepository.getAllMovies().first()
        val theaterId = TheaterRepository.addTheater(Theater(name = "Galaxy Cinema", location = "Uptown"))
        val audId = TheaterRepository.addAuditorium(Auditorium(number = 1, theaterId = theaterId))

        val startTime = LocalDateTime.of(2026, 6, 15, 19, 30)
        ShowtimeRepository.addShowtime(
            Showtime(
                movie = movie,
                startTime = startTime,
                auditoriumId = audId,
                seatingChart = List(5) { r -> List(10) { c -> Seat(r, c) } }
            )
        )

        val showtimes = ShowtimeRepository.getAllShowtimes()
        assertEquals(1, showtimes.size)
        assertEquals("Dune", showtimes[0].movie.name)
        assertEquals(startTime, showtimes[0].startTime)
        assertEquals(audId, showtimes[0].auditoriumId)
    }

    @Test
    fun `GUI delete showtime - removes it from the database`() {
        MovieRepository.addMovie(Movie(name = "Dune", durationMinutes = 155))
        val movie = MovieRepository.getAllMovies().first()
        val theaterId = TheaterRepository.addTheater(Theater(name = "Galaxy Cinema", location = "Uptown"))
        val audId = TheaterRepository.addAuditorium(Auditorium(number = 1, theaterId = theaterId))

        ShowtimeRepository.addShowtime(
            Showtime(
                movie = movie,
                startTime = LocalDateTime.of(2026, 6, 15, 19, 30),
                auditoriumId = audId,
                seatingChart = List(5) { r -> List(10) { c -> Seat(r, c) } }
            )
        )
        val id = ShowtimeRepository.getAllShowtimes().first().id

        assertTrue(ShowtimeRepository.deleteShowtime(id))
        assertTrue(ShowtimeRepository.getAllShowtimes().isEmpty())
    }
}
