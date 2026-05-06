package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.PaymentMethodTable
import edu.teamcandy.exposed.SeatTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.exposed.TheaterTable
import edu.teamcandy.exposed.TicketTable
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Theater
import edu.teamcandy.routes.defaultRoutes
import edu.teamcandy.routes.movieRoutes
import edu.teamcandy.routes.showtimeRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.Thymeleaf
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

private var isInitialized = false

fun init() {
    if (isInitialized) return
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable, TicketTable)

        // Manual migration for missing columns in movies table
        try {
            exec("ALTER TABLE movies ADD COLUMN cast TEXT DEFAULT ''")
        } catch (e: Exception) {
            // Column might already exist
        }
        try {
            exec("ALTER TABLE movies ADD COLUMN genres TEXT DEFAULT ''")
        } catch (e: Exception) {
            // Column might already exist
        }

        // Seed initial data if no theaters exist
        if (TheaterTable.selectAll().empty()) {
            val theaterId = TheaterTable.insert {
                it[TheaterTable.name] = "Candy Cinema"
                it[TheaterTable.location] = "Downtown"
            } get TheaterTable.id

            for (i in 1..4) {
                AuditoriumTable.insert {
                    it[AuditoriumTable.number] = i
                    it[AuditoriumTable.theaterId] = theaterId
                    it[AuditoriumTable.rows] = 5
                    it[AuditoriumTable.seatsPerRow] = 10
                }
            }

            // Seed initial movies
            val movies = listOf(
                Movie(
                    name = "Inception",
                    durationMinutes = 148,
                    rating = "PG-13",
                    cast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page"),
                    genres = listOf("Action", "Sci-Fi", "Adventure"),
                    description = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O."
                ),
                Movie(
                    name = "Interstellar",
                    durationMinutes = 169,
                    rating = "PG-13",
                    cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                    genres = listOf("Adventure", "Drama", "Sci-Fi"),
                    description = "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans."
                ),
                Movie(
                    name = "The Dark Knight",
                    durationMinutes = 152,
                    rating = "PG-13",
                    cast = listOf("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                    genres = listOf("Action", "Crime", "Drama"),
                    description = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice."
                )
            )

            val movieIds = movies.map { movie ->
                MovieTable.insert {
                    it[name] = movie.name
                    it[durationMinutes] = movie.durationMinutes
                    it[rating] = movie.rating
                    it[cast] = movie.cast.joinToString(",")
                    it[genres] = movie.genres.joinToString(",")
                    it[description] = movie.description
                } get MovieTable.id
            }

            // Seed initial showtimes
            val auditoriums = AuditoriumTable.selectAll().map { it[AuditoriumTable.id] }
            if (auditoriums.isNotEmpty() && movieIds.isNotEmpty()) {
                val now = LocalDateTime.now()
                movieIds.forEachIndexed { index, movieId ->
                    ShowtimeTable.insert {
                        it[movie] = movieId
                        it[startTime] = now.plusDays(index.toLong()).withHour(19).withMinute(0)
                        it[auditoriumId] = auditoriums[index % auditoriums.size]
                        it[unitPrice] = 12.00
                    }
                }
            }
        }
    }
    isInitialized = true
}

private var isApiStarted = false

fun startApiAndDatabase() {
    if (isApiStarted) return
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable, SeatTable, PaymentMethodTable, TicketTable)
        
        // Manual migration for missing columns in movies table
        try {
            exec("ALTER TABLE movies ADD COLUMN cast TEXT DEFAULT ''")
        } catch (e: Exception) {
            // Column might already exist
        }
        try {
            exec("ALTER TABLE movies ADD COLUMN genres TEXT DEFAULT ''")
        } catch (e: Exception) {
            // Column might already exist
        }
    }

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }
        install(Thymeleaf) {
            setTemplateResolver(ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "UTF-8"
            })
        }
        routing {
            defaultRoutes(ShowtimeRepository, MovieRepository)
            movieRoutes(MovieRepository)
            showtimeRoutes(ShowtimeRepository)
            swaggerUI(path = "swagger", swaggerFile = "openapi.json")
            staticResources("/css", "static/css")
            staticResources("/js", "static/js")
        }
        println("Web API is running at http://localhost:8080")
    }.start(wait = false)
    isApiStarted = true
}