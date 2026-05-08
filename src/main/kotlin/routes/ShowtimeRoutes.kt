package edu.teamcandy.routes

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.PaymentMethodTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.exposed.TicketTable
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Payment
import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.TicketResponse
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository.getAuditoriumById
import edu.teamcandy.services.exposed.TheaterRepository.getTheaterByAuditoriums
import edu.teamcandy.services.exposed.TheaterRepository.getTheaterById
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.imageio.ImageIO

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

                    val reservedSeats = TicketTable
                        .selectAll()
                        .where { TicketTable.showtimeId eq showtimeId }
                        .map {
                            Pair(
                                it[TicketTable.row],
                                it[TicketTable.seatNumber]
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

    get("/api/theatre/auditorium/{id}") {
        val auditoriumId = call.parameters["id"]?.toIntOrNull()
        if (auditoriumId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid auditorium ID")
            return@get
        }

        val theater = getTheaterByAuditoriums(auditoriumId)
        if (theater != null) {
            call.respond(theater)
        } else {
            call.respond(HttpStatusCode.NotFound, "Theater not found for auditorium ID $auditoriumId")
        }
    }

    get("/api/theatre/{id}") {
        val theaterId = call.parameters["id"]?.toIntOrNull()
        if (theaterId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid theater ID")
            return@get
        }

        val theater = getTheaterById(theaterId)
        if (theater != null) {
            call.respond(theater)
        } else {
            call.respond(HttpStatusCode.NotFound, "Theater not found")
        }
    }

    get("/api/auditorium/{id}") {
        val auditoriumID = call.parameters["id"]?.toIntOrNull()
        if (auditoriumID == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid auditorium ID")
            return@get
        }

        val auditorium = getAuditoriumById(auditoriumID)
        if (auditorium != null) {
            call.respond(auditorium)
        } else {
            call.respond(HttpStatusCode.NotFound, "Theater not found")
        }
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

        val seats = request.seats.map { Pair(it.row, it.number) }
        val confirmationCode = ShowtimeRepository.reserveSeats(showtimeId, seats)
        if (confirmationCode == null) {
            call.respond(HttpStatusCode.Conflict, "One or more seats are no longer available")
            return@post
        }

        val showtime = ShowtimeRepository.getAllShowtimes().firstOrNull { it.id == showtimeId }
        if (showtime == null) {
            call.respond(HttpStatusCode.NotFound, "Showtime not found")
            return@post
        }

        val bitMatrix = QRCodeWriter().encode(
            confirmationCode, BarcodeFormat.QR_CODE, 300, 300,
            mapOf(EncodeHintType.MARGIN to 1)
        )
        val baos = ByteArrayOutputStream()
        ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", baos)
        val qrBase64 = Base64.getEncoder().encodeToString(baos.toByteArray())

        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")
        val seatNames = request.seats.map { "${'A' + it.row}${it.number + 1}" }

        call.respond(TicketResponse(
            confirmationCode = confirmationCode,
            movieName = showtime.movie.name,
            startTime = showtime.startTime.format(fmt),
            seats = seatNames,
            totalPrice = seats.size * showtime.unitPrice,
            qrCodeBase64 = qrBase64
        ))
    }
}