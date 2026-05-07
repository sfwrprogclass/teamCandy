package edu.teamcandy.interfaces

import edu.teamcandy.models.Movie

interface MovieRepositoryInterface {
    fun getAllMovies(): List<Movie>
    fun deleteMovie(id: Int): Boolean
    fun addMovie(movie: Movie): Int
    fun updateMovie(id: Int, movie: Movie): Boolean
}
