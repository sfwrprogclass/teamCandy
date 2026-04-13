package edu.teamcandy.repository

import edu.teamcandy.models.Showtime

interface ShowtimeRepositoryInterface {
    fun getAllShowtimes(): List<Showtime>
    fun deleteShowtime(id: Int): Boolean
    fun addShowtime(showtime: Showtime)
    fun updateShowtime(id: Int, showtime: Showtime): Boolean
}
