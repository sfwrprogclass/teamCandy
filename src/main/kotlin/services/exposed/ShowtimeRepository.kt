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
import java.util.UUID

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
                    cast = it[MovieTable.cast].split(",").filter { s -> s.isNotBlank() },
                    genres = it[MovieTable.genres].split(",").filter { s -> s.isNotBlank() },
                    description = it[MovieTable.description]
                ),
                unitPrice = it[ShowtimeTable.unitPrice]
            )
        }.toList()
    }

    private fun generateConfirmationCode(): String {
        val hex = UUID.randomUUID().toString().replace("-", "").uppercase()
        return "${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}"
    }

    override fun reserveSeat(showtimeId: Int, row: Int, seatNumber: Int): String? = transaction {
        try {
            val code = generateConfirmationCode()
            TicketTable.insert {
                it[TicketTable.showtimeId] = showtimeId
                it[TicketTable.row] = row
                it[TicketTable.seatNumber] = seatNumber
                it[TicketTable.soldAt] = LocalDateTime.now()
                it[TicketTable.confirmationCode] = code
            }
            code
        } catch (e: Exception) {
            null
        }
    }

    override fun reserveSeats(showtimeId: Int, seats: List<Pair<Int, Int>>): String? = transaction {
        try {
            val code = generateConfirmationCode()
            val now = LocalDateTime.now()
            TicketTable.batchInsert(seats) { (r, c) ->
                this[TicketTable.showtimeId] = showtimeId
                this[TicketTable.row] = r
                this[TicketTable.seatNumber] = c
                this[TicketTable.soldAt] = now
                this[TicketTable.confirmationCode] = code
            }
            code
        } catch (e: Exception) {
            rollback()
            null
        }
    }

    override fun addShowtime(showtime: Showtime): Int? = transaction {
        val newStart = showtime.startTime
        val newEnd = showtime.endTime

        val hasConflict = (ShowtimeTable innerJoin MovieTable)
            .selectAll()
            .where { ShowtimeTable.auditoriumId eq showtime.auditoriumId }
            .any { row ->
                val existingStart = row[ShowtimeTable.startTime]
                val existingEnd = existingStart.plusMinutes(
                    (row[MovieTable.durationMinutes] + row[ShowtimeTable.paddingMinutes]).toLong()
                )
                newStart < existingEnd && existingStart < newEnd
            }

        if (hasConflict) return@transaction null

        ShowtimeTable.insert {
            it[movie] = showtime.movie.id
            it[startTime] = showtime.startTime
            it[paddingMinutes] = showtime.paddingMinutes
            it[auditoriumId] = showtime.auditoriumId
            it[unitPrice] = showtime.unitPrice
        } get ShowtimeTable.id
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

    override fun getTicketsSoldByTheaterAndDateRange(
        theaterId: Int,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Pair<Showtime, Int>> = transaction {
        val ticketCount = TicketTable.id.count()
        (TicketTable innerJoin ShowtimeTable innerJoin AuditoriumTable innerJoin MovieTable)
            .slice(
                ShowtimeTable.id, ShowtimeTable.startTime, ShowtimeTable.paddingMinutes, ShowtimeTable.auditoriumId, ShowtimeTable.unitPrice,
                MovieTable.id, MovieTable.name, MovieTable.durationMinutes, MovieTable.rating, MovieTable.cast, MovieTable.genres, MovieTable.description,
                ticketCount
            )
            .select {
                (AuditoriumTable.theaterId eq theaterId) and
                (TicketTable.soldAt greaterEq startDate) and
                (TicketTable.soldAt lessEq endDate)
            }
            .groupBy(
                ShowtimeTable.id, ShowtimeTable.startTime, ShowtimeTable.paddingMinutes, ShowtimeTable.auditoriumId, ShowtimeTable.unitPrice,
                MovieTable.id, MovieTable.name, MovieTable.durationMinutes, MovieTable.rating, MovieTable.cast, MovieTable.genres, MovieTable.description
            )
            .map {
                val showtime = Showtime(
                    id = it[ShowtimeTable.id],
                    startTime = it[ShowtimeTable.startTime],
                    paddingMinutes = it[ShowtimeTable.paddingMinutes],
                    auditoriumId = it[ShowtimeTable.auditoriumId],
                    seatingChart = emptyList(), // Not needed for report
                    movie = Movie(
                        id = it[MovieTable.id],
                        name = it[MovieTable.name],
                        durationMinutes = it[MovieTable.durationMinutes],
                        rating = it[MovieTable.rating],
                        cast = it[MovieTable.cast].split(",").filter { s -> s.isNotBlank() },
                        genres = it[MovieTable.genres].split(",").filter { s -> s.isNotBlank() },
                        description = it[MovieTable.description]
                    ),
                    unitPrice = it[ShowtimeTable.unitPrice]
                )
                showtime to it[ticketCount].toInt()
            }
    }
}
