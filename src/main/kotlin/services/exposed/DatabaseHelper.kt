package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.*
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Theater
import edu.teamcandy.routes.defaultRoutes
import edu.teamcandy.routes.movieRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import io.ktor.server.thymeleaf.Thymeleaf
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction

fun init() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable, TicketTable)

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
        }

        // Seed movies if none exist or if they are "empty"
        val existingMovies = MovieTable.selectAll().toList()
        if (existingMovies.isEmpty() || existingMovies.any { it[MovieTable.name].isBlank() }) {
            // Clear existing if any "empty" ones exist to start fresh
            if (existingMovies.isNotEmpty()) {
                ShowtimeTable.deleteWhere { ShowtimeTable.id.isNotNull() }
                MovieTable.deleteWhere { MovieTable.id.isNotNull() }
            }

            val movieRepo = edu.teamcandy.repositories.MovieRepository()
            val allMovies = movieRepo.getAllMovies()
            val auditoriumId = AuditoriumTable.selectAll().firstOrNull()?.get(AuditoriumTable.id)

            allMovies.forEach { movie ->
                val movieId = MovieTable.insert {
                    it[name] = movie.name
                    it[durationMinutes] = movie.durationMinutes
                    it[rating] = movie.rating
                    it[description] = movie.description
                } get MovieTable.id

                // Optionally seed some showtimes for the first few movies
                if (movie.id <= 3 && auditoriumId != null) {
                    ShowtimeTable.insert {
                        it[ShowtimeTable.movie] = movieId
                        it[startTime] = java.time.LocalDateTime.now().plusHours(movie.id.toLong() * 2)
                        it[paddingMinutes] = 15
                        it[ShowtimeTable.auditoriumId] = auditoriumId
                    }
                }
            }
        }
    }
}
fun startApiAndDatabase() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable, TicketTable)
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
swaggerUI(path = "swagger", swaggerFile = "openapi.json")
        }
        println("Web API is running at http://localhost:8080")
    }.start(wait = false)
}