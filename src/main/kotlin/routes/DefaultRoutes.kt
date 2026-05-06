package edu.teamcandy.routes

import edu.teamcandy.repository.ShowtimeRepositoryInterface
import edu.teamcandy.repository.MovieRepositoryInterface
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.thymeleaf.ThymeleafContent
import java.time.LocalDate

fun Route.defaultRoutes(showTimeRepository: ShowtimeRepositoryInterface,
                        movieRepository: MovieRepositoryInterface) {
    route("/") {
        get {
            val today = LocalDate.now()
            val maxDate = today.plusDays(6)

            val selectedDate = call.request.queryParameters["date"]
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?.coerceIn(today, maxDate)
                ?: today

            val showtimes = showTimeRepository.getAllShowtimes()
            val showtimesByMovie = movieRepository.getAllMovies()
                .associateWith { movie ->
                    showtimes.filter { it.movie.id == movie.id && it.startTime.toLocalDate() == selectedDate }
                }
                .filter { (_, movieShowtimes) -> movieShowtimes.isNotEmpty() }

            call.respond(ThymeleafContent("index", mapOf(
                "showtimesByMovie" to showtimesByMovie,
                "selectedDate" to selectedDate,
                "prevDate" to if (selectedDate > today) selectedDate.minusDays(1) else null,
                "nextDate" to if (selectedDate < maxDate) selectedDate.plusDays(1) else null
            )))
        }
    }
}