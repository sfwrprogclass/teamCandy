package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.exposed.TheaterTable
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun init() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable)

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
    }
}
fun startApiAndDatabase() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable, TheaterTable, AuditoriumTable)
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