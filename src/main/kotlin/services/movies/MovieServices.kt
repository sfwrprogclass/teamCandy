package edu.teamcandy.services.movies

import edu.teamcandy.models.Movie
import edu.teamcandy.services.exposed.MovieRepository
import kotlin.math.round

class MovieServices {
    fun showOptions() {
        var input: Int?
        do {
            println("What would you like to do?:")
            println("0. Back")
            println("1. View Movies")
            println("2. Add Movie")
            println("3. Edit Movie")
            println("4. Delete Movie")
            print("Enter option (0-4): ")
            input = readlnOrNull()?.toIntOrNull()

            when (input) {
                1 -> viewMovies()
                2 -> addMovie()
                3 -> updateMovie()
                4 -> deleteMovie()
                0 -> println("Goodbye!")
                else -> println("Please enter 0-4.")
            }
        } while (input != 0)
    }

    fun addMovie() {
        println("Add New Movie")

        print("Enter a movie title: ")
        val movieName = readln()

        print("Enter the movie description: ")
        val movieDescription = readln()

        print("Enter the movie duration in minutes: ")
        val movieDuration = readln().toIntOrNull() ?: 0

        var movieRating: Double
        do {
            print("Enter the rating of the movie (1-10): ")
            val ratingInput = readln().toDoubleOrNull()
            if (ratingInput != null && ratingInput in 1.0..10.0) {
                movieRating = round(ratingInput * 10) / 10
                break
            } else {
                println("Rating should be a number between 1 and 10")
            }
        } while (true)

        val movie = Movie(name = movieName, durationMinutes = movieDuration, rating = movieRating.toString(), description = movieDescription)
        MovieRepository.addMovie(movie)

        println("Movie has been added!!")
    }

    fun updateMovie() {
        val movies = MovieRepository.getAllMovies()
        if (movies.isEmpty()) {
            println("No movies found in database.")
            return
        }

        println("\nSelect a movie to edit:")
        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (ID: ${movie.id})")
        }

        print("Enter the number: ")
        val choice = readlnOrNull()?.toIntOrNull() ?: -1

        if (choice in 1..movies.size) {
            val selectedMovie = movies[choice - 1]
            println("Editing '${selectedMovie.name}'")

            print("New name (blank to keep '${selectedMovie.name}'): ")
            val newName = readln()
            if (newName.isNotBlank()) selectedMovie.name = newName

            print("New description (blank to keep current): ")
            val newDesc = readln()
            if (newDesc.isNotBlank()) selectedMovie.description = newDesc

            print("New duration (blank to keep ${selectedMovie.durationMinutes}): ")
            val newDur = readln()
            if (newDur.isNotBlank()) selectedMovie.durationMinutes = newDur.toIntOrNull() ?: selectedMovie.durationMinutes

            // UPDATE IN DATABASE
            val success = MovieRepository.updateMovie(selectedMovie.id, selectedMovie)
            if (success) println("Movie updated successfully!") else println("Update failed.")
        } else {
            println("Invalid choice.")
        }
    }

    fun viewMovies() {
        // FETCH FROM DATABASE
        val movies = MovieRepository.getAllMovies()
        if (movies.isEmpty()) {
            println("No movies found.")
            return
        }

        println("Movie List")
        movies.forEach { movie ->
            println("ID: ${movie.id} | ${movie.name} | Rating: ${movie.rating} | ${movie.durationMinutes} mins")
            println("Description: ${movie.description}\n")
        }
    }

    fun deleteMovie() {
        val movies = MovieRepository.getAllMovies()
        if (movies.isEmpty()) {
            println("No movies to delete.")
            return
        }

        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name}")
        }

        print("Enter the number of the movie to delete: ")
        val choice = readlnOrNull()?.toIntOrNull() ?: -1

        if (choice in 1..movies.size) {
            val movieToDelete = movies[choice - 1]

            // DELETE FROM DATABASE
            val success = MovieRepository.deleteMovie(movieToDelete.id)
            if (success) println("Deleted: ${movieToDelete.name}") else println("Delete failed.")
        } else {
            println("Invalid selection.")
        }
    }
}
