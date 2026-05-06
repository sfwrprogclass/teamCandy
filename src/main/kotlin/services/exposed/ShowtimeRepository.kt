package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.exposed.ShowtimeTable
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Showtime
import edu.teamcandy.repository.ShowtimeRepositoryInterface
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ShowtimeRepository : ShowtimeRepositoryInterface {

    override fun getAllShowtimes(): List<Showtime> = transaction {
        (ShowtimeTable innerJoin MovieTable)
            .selectAll()
            .map {
                Showtime(
                    id = it[ShowtimeTable.id],
                    startTime = it[ShowtimeTable.startTime],
                    paddingMinutes = it[ShowtimeTable.paddingMinutes].toLong(),
                    movie = Movie(
                        id = it[MovieTable.id],
                        name = it[MovieTable.name],
                        durationMinutes = it[MovieTable.durationMinutes],
                        rating = it[MovieTable.rating],
                        description = it[MovieTable.description],
                        imageUrl = it[MovieTable.imageUrl]
                    )
                )
            }
    }

    override fun addShowtime(showtime: Showtime) {
        transaction {
            ShowtimeTable.insert {
                it[movie] = showtime.movie.id
                it[startTime] = showtime.startTime
                it[paddingMinutes] = showtime.paddingMinutes.toInt()
            }
        }
    }

    override fun updateShowtime(id: Int, showtime: Showtime): Boolean = transaction {
        ShowtimeTable.update({ ShowtimeTable.id eq id }) {
            it[movie] = showtime.movie.id
            it[startTime] = showtime.startTime
            it[paddingMinutes] = showtime.paddingMinutes.toInt()
        } > 0
    }

    override fun deleteShowtime(id: Int): Boolean = transaction {
        ShowtimeTable.deleteWhere { ShowtimeTable.id eq id } > 0
    }
}
