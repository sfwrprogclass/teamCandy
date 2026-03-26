package edu.teamcandy.services

import edu.teamcandy.models.Showtime

class BookingService {
    fun bookTicket(showtime: Showtime, row: Int, seatNumber: Int): String {
        return if (showtime.reserveSeat(row, seatNumber)) {
            "Ticket booked successfully for ${showtime.movie.name} at ${'A' + row}${seatNumber + 1}."
        } else {
            "Failed to book ticket. Seat ${'A' + row}${seatNumber + 1} is already reserved or invalid."
        }
    }

    fun sellTicket(showtime: Showtime, row: Int, seatNumber: Int): String {
        // Employees sell tickets (similar to booking but maybe different logging/payment)
        return if (showtime.reserveSeat(row, seatNumber)) {
            "Ticket sold successfully for ${showtime.movie.name} at ${'A' + row}${seatNumber + 1}."
        } else {
            "Failed to sell ticket. Seat ${'A' + row}${seatNumber + 1} is already reserved or invalid."
        }
    }
}
