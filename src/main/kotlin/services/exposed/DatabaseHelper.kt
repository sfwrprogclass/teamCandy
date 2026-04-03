package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.ShowtimeTable
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun init() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable)
    }
}
fun startApiAndDatabase() {
    Database.connect("jdbc:sqlite:./theater.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(MovieTable, ShowtimeTable)
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
        routing {
            movieRoutes(MovieRepository)
            swaggerUI(path = "swagger", swaggerFile = "openapi.json")
        }
        println("Web API is running at http://localhost:8080")
    }.start(wait = false)
}