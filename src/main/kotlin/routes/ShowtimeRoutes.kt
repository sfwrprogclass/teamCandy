package edu.teamcandy.routes

import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.PaymentMethodTable
import edu.teamcandy.exposed.SeatTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Payment
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.showtimeRoutes(showtimeRepositoryInterface: ShowtimeRepositoryInterface) {
    get("/api/movies/{id}/showtimes") {
        val movieId = call.parameters["id"]?.toIntOrNull()
        val dateParam = call.request.queryParameters["date"]

        if (movieId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid movie id")
            return@get
        }

        if (dateParam == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing date")
            return@get
        }

        val date = try {
            java.time.LocalDate.parse(dateParam)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid date format")
            return@get
        }

        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay()

        val showtimes = transaction {
            (ShowtimeTable innerJoin MovieTable innerJoin AuditoriumTable)
                .selectAll()
                .where {
                    (ShowtimeTable.movie eq movieId) and
                            (ShowtimeTable.startTime greaterEq startOfDay) and
                            (ShowtimeTable.startTime less endOfDay)
                }
                .map {
                    val movie = Movie(
                        id = it[MovieTable.id],
                        name = it[MovieTable.name],
                        durationMinutes = it[MovieTable.durationMinutes],
                        rating = it[MovieTable.rating],
                        description = it[MovieTable.description]
                    )

                    val showtimeId = it[ShowtimeTable.id]
                    val rows = it[AuditoriumTable.rows]
                    val seatsPerRow = it[AuditoriumTable.seatsPerRow]

                    val reservedSeats = SeatTable
                        .selectAll()
                        .where { SeatTable.showtimeId eq showtimeId }
                        .map {
                            Pair(
                                it[SeatTable.row],
                                it[SeatTable.seatNumber]
                            )
                        }
                        .toSet()

                    val seatingChart = List(rows) { rowIndex ->
                        List(seatsPerRow) { seatIndex ->
                            Seat(
                                row = rowIndex,
                                number = seatIndex,
                                isReserved = reservedSeats.contains(Pair(rowIndex, seatIndex))
                            )
                        }
                    }

                    Showtime(
                        id = it[ShowtimeTable.id],
                        movie = movie,
                        startTime = it[ShowtimeTable.startTime],
                        auditoriumId = it[ShowtimeTable.auditoriumId],
                        seatingChart = seatingChart,
                        paddingMinutes = it[ShowtimeTable.paddingMinutes],
                        unitPrice = it[ShowtimeTable.unitPrice],
                    )
                }
        }

        call.respond(showtimes)
    }

    post("/api/showtimes/{id}/reserve") {
        val showtimeId = call.parameters["id"]?.toIntOrNull()

        if (showtimeId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid showtime id")
            return@post
        }

        val seats = call.receive<List<Seat>>()

        transaction {
            for (seat in seats) {
                val alreadyReserved = SeatTable
                    .selectAll()
                    .where {
                        (SeatTable.showtimeId eq showtimeId) and
                                (SeatTable.row eq seat.row) and
                                (SeatTable.seatNumber eq seat.number)
                    }
                    .count() > 0

                if (!alreadyReserved) {
                    SeatTable.insert {
                        it[SeatTable.showtimeId] = showtimeId
                        it[SeatTable.row] = seat.row
                        it[SeatTable.seatNumber] = seat.number
                    }
                }
            }
        }

        call.respond(HttpStatusCode.OK, "Seats reserved successfully")
    }

    post("/api/showtimes/{id}/pay-and-reserve") {
        val showtimeId = call.parameters["id"]?.toIntOrNull()

        if (showtimeId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid showtime id")
            return@post
        }

        val request = call.receive<Payment>()

        val paymentMatches = transaction {
            PaymentMethodTable
                .selectAll()
                .where {
                    (PaymentMethodTable.nameOnCard eq request.nameOnCard) and
                            (PaymentMethodTable.cardNumber eq request.cardNumber) and
                            (PaymentMethodTable.expiry eq request.expiry) and
                            (PaymentMethodTable.cvv eq request.cvv)
                }
                .count() > 0
        }

        if (!paymentMatches) {
            call.respond(HttpStatusCode.Unauthorized, "Payment method does not match")
            return@post
        }

        transaction {
            for (seat in request.seats) {
                val alreadyReserved = SeatTable
                    .selectAll()
                    .where {
                        (SeatTable.showtimeId eq showtimeId) and
                                (SeatTable.row eq seat.row) and
                                (SeatTable.seatNumber eq seat.number)
                    }
                    .count() > 0

                if (!alreadyReserved) {
                    SeatTable.insert {
                        it[SeatTable.showtimeId] = showtimeId
                        it[SeatTable.row] = seat.row
                        it[SeatTable.seatNumber] = seat.number
                    }
                }
            }
        }

        call.respond(HttpStatusCode.OK, "Payment accepted and seats reserved")
    }
}