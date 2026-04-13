package Mainframe.Maincode.Admin

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object MovieRepository {

    fun addMovie(movie: Movie) {
        transaction {
            MoviesTable.insert {
                it[title] = movie.title
                it[duration] = movie.duration
                it[description] = movie.description
                it[theater] = movie.theater
                it[poster] = movie.poster
            }
        }
    }

    fun listMovies(): List<Movie> {
        return transaction {
            MoviesTable.selectAll().map {
                Movie(
                    title = it[MoviesTable.title],
                    duration = it[MoviesTable.duration],
                    description = it[MoviesTable.description],
                    theater = it[MoviesTable.theater],
                    poster = it[MoviesTable.poster]
                )
            }
        }
    }
}
