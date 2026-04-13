package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.MovieTable
import edu.teamcandy.models.Movie
import edu.teamcandy.repository.MovieRepositoryInterface
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object MovieRepository : MovieRepositoryInterface {

    override fun addMovie(movie: Movie) {
        transaction {
            MovieTable.insert {
                it[name] = movie.name
                it[durationMinutes] = movie.durationMinutes
                it[rating] = movie.rating
                it[description] = movie.description
            }
        }
        println("Saved ${movie.name} to the database!")
    }

    override fun getAllMovies(): List<Movie> = transaction {
        MovieTable.selectAll().map {
            Movie(
                id = it[MovieTable.id],
                name = it[MovieTable.name],
                durationMinutes = it[MovieTable.durationMinutes],
                rating = it[MovieTable.rating],
                description = it[MovieTable.description]
            )
        }.toList()
    }

    override fun updateMovie(id: Int, updatedMovie: Movie): Boolean = transaction {
        val rowsUpdated = MovieTable.update({ MovieTable.id eq id }) {
            it[name] = updatedMovie.name
            it[durationMinutes] = updatedMovie.durationMinutes
            it[rating] = updatedMovie.rating
            it[description] = updatedMovie.description
        }
        rowsUpdated > 0
    }

    override fun deleteMovie(id: Int): Boolean = transaction {
        val rowsDeleted = MovieTable.deleteWhere { MovieTable.id eq id }
        rowsDeleted > 0
    }
}
