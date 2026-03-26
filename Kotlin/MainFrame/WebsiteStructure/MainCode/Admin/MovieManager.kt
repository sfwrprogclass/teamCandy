package Mainframe.Maincode.Admin


import Mainframe.Maincode.Admin.Movie

object MovieManager {
    private val movies = mutableListOf<Movie>()

    fun addMovie(movie: Movie) {
        movies.add(movie)
    }

    fun listMovies(): List<Movie> = movies
}
