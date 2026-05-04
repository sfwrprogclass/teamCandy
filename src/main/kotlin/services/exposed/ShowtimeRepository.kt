package edu.teamcandy.services.exposed

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Showtime
import edu.teamcandy.exposed.*
import edu.teamcandy.models.Seat
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object ShowtimeRepository : ShowtimeRepositoryInterface {

    override fun getAllShowtimes(): List<Showtime> = transaction {
        val tickets = TicketTable.selectAll().toList()

        (ShowtimeTable innerJoin MovieTable innerJoin AuditoriumTable).selectAll().map {
            val showId = it[ShowtimeTable.id]
            val rows = it[AuditoriumTable.rows]
            val seatsPerRow = it[AuditoriumTable.seatsPerRow]

            val showtimeTickets = tickets.filter { t -> t[TicketTable.showtimeId] == showId }

            val seatingChart = List(rows) { r ->
                List(seatsPerRow) { c ->
                    Seat(r, c, isReserved = showtimeTickets.any { t -> t[TicketTable.row] == r && t[TicketTable.seatNumber] == c })
                }
            }

            Showtime(
                id = showId,
                startTime = it[ShowtimeTable.startTime],
                paddingMinutes = it[ShowtimeTable.paddingMinutes],
                auditoriumId = it[ShowtimeTable.auditoriumId],
                seatingChart = seatingChart,
                movie = Movie(
                    id = it[MovieTable.id],
                    name = it[MovieTable.name],
                    durationMinutes = it[MovieTable.durationMinutes],
                    rating = it[MovieTable.rating],
                    description = it[MovieTable.description]
                ),
                unitPrice = it[ShowtimeTable.unitPrice]
            )
        }.toList()
    }

    override fun reserveSeat(showtimeId: Int, row: Int, seatNumber: Int): Boolean = transaction {
        try {
            TicketTable.insert {
                it[TicketTable.showtimeId] = showtimeId
                it[TicketTable.row] = row
                it[TicketTable.seatNumber] = seatNumber
                it[TicketTable.soldAt] = LocalDateTime.now()
            }
            true
        } catch (e: Exception) {
            // Unique constraint violation or other error
            false
        }
    }

    override fun addShowtime(showtime: Showtime) {
        transaction {
            ShowtimeTable.insert {
                it[movie] = showtime.movie.id
                it[startTime] = showtime.startTime
                it[paddingMinutes] = showtime.paddingMinutes
                it[auditoriumId] = showtime.auditoriumId
            }
        }
    }

    override fun updateShowtime(id: Int, showtime: Showtime): Boolean = transaction {
        val rowsUpdated = ShowtimeTable.update({ ShowtimeTable.id eq id }) {
            it[movie] = showtime.movie.id
            it[startTime] = showtime.startTime
            it[paddingMinutes] = showtime.paddingMinutes
            it[auditoriumId] = showtime.auditoriumId
        }
        rowsUpdated > 0
    }

    override fun deleteShowtime(id: Int): Boolean = transaction {
        val rowsDeleted = ShowtimeTable.deleteWhere { ShowtimeTable.id eq id }
        rowsDeleted > 0
    }
}
