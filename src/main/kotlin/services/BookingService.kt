package edu.teamcandy.services

import edu.teamcandy.models.Showtime
import edu.teamcandy.models.TicketReceipt
import edu.teamcandy.services.exposed.ShowtimeRepository
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")

class BookingService {
    fun bookTicket(showtime: Showtime, row: Int, seatNumber: Int): TicketReceipt? {
        val code = ShowtimeRepository.reserveSeat(showtime.id, row, seatNumber) ?: return null
        showtime.reserveSeat(row, seatNumber)
        return TicketReceipt(
            confirmationCode = code,
            movieName = showtime.movie.name,
            startTime = showtime.startTime.format(DISPLAY_FORMAT),
            seatNames = listOf("${'A' + row}${seatNumber + 1}"),
            totalPrice = showtime.unitPrice
        )
    }

    fun sellTicket(showtime: Showtime, row: Int, seatNumber: Int): TicketReceipt? {
        val code = ShowtimeRepository.reserveSeat(showtime.id, row, seatNumber) ?: return null
        showtime.reserveSeat(row, seatNumber)
        return TicketReceipt(
            confirmationCode = code,
            movieName = showtime.movie.name,
            startTime = showtime.startTime.format(DISPLAY_FORMAT),
            seatNames = listOf("${'A' + row}${seatNumber + 1}"),
            totalPrice = showtime.unitPrice
        )
    }

    fun sellTickets(showtime: Showtime, seats: List<Pair<Int, Int>>): TicketReceipt? {
        val code = ShowtimeRepository.reserveSeats(showtime.id, seats) ?: return null
        seats.forEach { (r, c) -> showtime.reserveSeat(r, c) }
        return TicketReceipt(
            confirmationCode = code,
            movieName = showtime.movie.name,
            startTime = showtime.startTime.format(DISPLAY_FORMAT),
            seatNames = seats.map { (r, c) -> "${'A' + r}${c + 1}" },
            totalPrice = seats.size * showtime.unitPrice
        )
    }
}
