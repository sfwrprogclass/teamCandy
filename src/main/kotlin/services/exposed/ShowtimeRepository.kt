package edu.teamcandy.services.exposed

import edu.teamcandy.models.Movie
import edu.teamcandy.models.Showtime
import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

object ShowtimeRepository : ShowtimeRepositoryInterface {

    override fun getAllShowtimes(): List<Showtime> = transaction {
        (ShowtimeTable innerJoin MovieTable).selectAll().map {
            Showtime(
                id = it[ShowtimeTable.id],
                startTime = it[ShowtimeTable.startTime],
                paddingMinutes = it[ShowtimeTable.paddingMinutes],
                auditoriumId = it[ShowtimeTable.auditoriumId],
                seatingChart = List(5) { r -> List(10) { c -> edu.teamcandy.models.Seat(r, c) } },
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
