package edu.teamcandy.repository

import edu.teamcandy.models.Showtime
import java.time.LocalDateTime

interface ShowtimeRepositoryInterface {
    fun getAllShowtimes(): List<Showtime>
    fun deleteShowtime(id: Int): Boolean
    fun addShowtime(showtime: Showtime): Int
    fun updateShowtime(id: Int, showtime: Showtime): Boolean
    fun reserveSeat(showtimeId: Int, row: Int, seatNumber: Int): Boolean
    fun reserveSeats(showtimeId: Int, seats: List<Pair<Int, Int>>): Boolean
    fun getTicketsSoldByTheaterAndDateRange(theaterId: Int, startDate: LocalDateTime, endDate: LocalDateTime): List<Pair<Showtime, Int>>
}
