package Mainframe.Maincode.Admin

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.http.content.*
import io.ktor.http.*
import java.io.File
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// --- Data class ---
data class Movie(
    val title: String,
    val duration: Int,
    val description: String,
    val theater: String,
    val poster: String
)

// --- Exposed table ---
object MoviesTable : Table("movies") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val duration = integer("duration")
    val description = text("description")
    val theater = varchar("theater", 255)
    val poster = varchar("poster", 255)

    override val primaryKey = PrimaryKey(id)
}

// --- Repository ---
object MovieRepository {

    fun addMovie(movie: Movie) {
        transaction {
            MoviesTable.insert {
                it[title] = movie.title
                it[duration] = movie.duration
                it[description] = movie.description
                it[theater] = movie.theater
                it[poster] = movie.poster
            }
        }
    }

    fun listMovies(): List<Movie> {
        return transaction {
            MoviesTable.selectAll().map {
                Movie(
                    title = it[MoviesTable.title],
                    duration = it[MoviesTable.duration],
                    description = it[MoviesTable.description],
                    theater = it[MoviesTable.theater],
                    poster = it[MoviesTable.poster]
                )
            }
        }
    }
}

// --- Server ---
fun startServer() {
    embeddedServer(Netty, port = 8080) {

        // Database init
        Database.connect("jdbc:sqlite:movies.db", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(MoviesTable)
        }

        install(ContentNegotiation) { gson() }

        routing {

            // Serve images
            static("/Images") {
                files("Images")
            }

            // Add movie (Base64 JSON)
            post("/addMovie") {
                val data = call.receive<Map<String, String>>()

                val title = data["title"]!!
                val duration = data["duration"]!!.toInt()
                val description = data["description"]!!
                val theater = data["theater"]!!
                val posterBase64 = data["posterBase64"]!!
                val posterName = data["posterName"]!!

                // Decode Base64 → bytes
                val bytes = java.util.Base64.getDecoder().decode(posterBase64)

                // Save file
                val folder = File("Images")
                if (!folder.exists()) folder.mkdirs()
                File(folder, posterName).writeBytes(bytes)

                val movie = Movie(
                    title = title,
                    duration = duration,
                    description = description,
                    theater = theater,
                    poster = "/Images/$posterName"
                )

                MovieRepository.addMovie(movie)
                call.respond(HttpStatusCode.OK, movie)
            }

            // List movies
            get("/movies") {
                call.respond(MovieRepository.listMovies())
            }

            // Login
            post("/login") {
                val credentials = call.receive<Map<String, String>>()
                val username = credentials["username"]
                val password = credentials["password"]

                if (username == "admin" && password == "password123") {
                    call.respond(mapOf("success" to true))
                } else {
                    call.respond(mapOf("success" to false))
                }
            }
        }

    }.start(wait = false)
}
