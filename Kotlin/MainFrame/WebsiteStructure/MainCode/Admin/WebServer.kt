package Mainframe.Maincode.Admin

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*

fun startServer() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }

        routing {
            post("/addMovie") {
                val movie = call.receive<Movie>()
                MovieManager.addMovie(movie)
                call.respond("Movie added")
            }

            get("/movies") {
                call.respond(MovieManager.listMovies())
            }
        }
    }.start(wait = false)
}
