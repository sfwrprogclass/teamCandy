package edu.teamcandy.routes

import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.models.Movie
import edu.teamcandy.interfaces.MovieRepositoryInterface
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.thymeleaf.ThymeleafContent
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.movieRoutes(repository: MovieRepositoryInterface) {
    route("/api/movies") {

        get {
            val movies: List<Movie> = repository.getAllMovies()
            call.respond<List<Movie>>(movies)
        }

        post {
            try {
                val movie = call.receive<Movie>()
                repository.addMovie(movie)
                call.respond(HttpStatusCode.Created, "Movie added to database")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Movie Data")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            try {
                val updatedMovie = call.receive<Movie>()
                if (repository.updateMovie(id, updatedMovie)) {
                    call.respond(HttpStatusCode.OK, "Movie updated")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Movie ID not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Update Data")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID format")

            if (repository.deleteMovie(id)) {
                call.respond(HttpStatusCode.OK, "Movie deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Movie not found")
            }
        }
    }

    get("/movies/{id}") {
        val movieId = call.parameters["id"]?.toIntOrNull()

        if (movieId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid movie id")
            return@get
        }

        val movie = transaction {
            MovieTable
                .selectAll()
                .where { MovieTable.id eq movieId }
                .singleOrNull()
                ?.let {
                    Movie(
                        id = it[MovieTable.id],
                        name = it[MovieTable.name],
                        durationMinutes = it[MovieTable.durationMinutes],
                        rating = it[MovieTable.rating],
                        description = it[MovieTable.description]
                    )
                }
        }

        if (movie == null) {
            call.respond(HttpStatusCode.NotFound, "Movie not found")
            return@get
        }

        call.respond(
            ThymeleafContent(
                "customer/MovieDetails",
                mapOf("movie" to movie)
            )
        )
    }
}