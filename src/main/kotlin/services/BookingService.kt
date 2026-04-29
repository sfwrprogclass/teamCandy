package edu.teamcandy.services

import edu.teamcandy.models.Showtime
import edu.teamcandy.services.exposed.ShowtimeRepository

class BookingService {
    fun bookTicket(showtime: Showtime, row: Int, seatNumber: Int): String {
        val success = ShowtimeRepository.reserveSeat(showtime.id, row, seatNumber)
        return if (success) {
            showtime.reserveSeat(row, seatNumber) // Keep in-memory in sync if needed, though repo is source of truth
            "Ticket booked successfully for ${showtime.movie.name} at ${'A' + row}${seatNumber + 1}."
        } else {
            "Failed to book ticket. Seat ${'A' + row}${seatNumber + 1} is already reserved or invalid."
        }
    }

    fun sellTicket(showtime: Showtime, row: Int, seatNumber: Int): String {
        // Employees sell tickets (similar to booking but maybe different logging/payment)
        val success = ShowtimeRepository.reserveSeat(showtime.id, row, seatNumber)
        return if (success) {
            showtime.reserveSeat(row, seatNumber)
            "Ticket sold successfully for ${showtime.movie.name} at ${'A' + row}${seatNumber + 1}."
        } else {
            "Failed to sell ticket. Seat ${'A' + row}${seatNumber + 1} is already reserved or invalid."
        }
    }
}
