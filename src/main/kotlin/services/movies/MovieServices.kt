package edu.teamcandy.services.movies

import edu.teamcandy.models.Movie
import java.io.File
import kotlin.math.round

class MovieServices {

    fun showOptions() {
        var input: Int?
        do {
            println("What would you like to do?:")
            println("0. Exit")
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
        val file = File("movies.csv")
        val movies = getMoviesFromFile(file).toMutableList()

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

        val movie = Movie(movieName, movieDuration, movieRating.toString(), movieDescription)
        movies.add(movie)

        saveMoviesToFile(file, movies)
        println("Movie has been added!!")
    }

    fun updateMovie() {
        val file = File("movies.csv")
        val movies = getMoviesFromFile(file).toMutableList()
        if (movies.isEmpty()) {
            println("No movies found.")
            return
        }

        // Show numbered list
        println("Movie List")
        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (${movie.durationMinutes} min, Rating: ${movie.rating})")
        }

        var choice: Int
        do {
            print("Enter the number of the movie to edit: ")
            choice = readlnOrNull()?.toIntOrNull() ?: -1
        } while (choice !in 1..movies.size)

        val movie = movies[choice - 1]
        println("Editing '${movie.name}'")

        print("New name (leave blank to keep current): ")
        val newName = readln()
        if (newName.isNotBlank()) movie.name = newName

        print("New description (leave blank to keep current): ")
        val newDesc = readln()
        if (newDesc.isNotBlank()) movie.description = newDesc

        print("New duration in minutes (leave blank to keep current): ")
        val newDurationInput = readln()
        if (newDurationInput.isNotBlank()) {
            movie.durationMinutes = newDurationInput.toIntOrNull() ?: movie.durationMinutes
        }

        var newRatingInput: String
        do {
            print("New rating (1-10) (leave blank to keep current): ")
            newRatingInput = readln()
            if (newRatingInput.isBlank()) break

            val rating = newRatingInput.toDoubleOrNull()
            if (rating != null && rating in 1.0..10.0) {
                movie.rating = round((rating * 10) / 10).toString()
                break
            } else {
                println("Invalid rating. Must be between 1 and 10.")
            }
        } while (true)

        saveMoviesToFile(file, movies)
        println("Movie updated successfully!")
    }

    fun viewMovies() {
        val movies = getMoviesFromFile(File("movies.csv"))
        if (movies.isEmpty()) {
            println("No movies found.")
            return
        }

        println("Movie List")
        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (${movie.durationMinutes} min, Rating: ${movie.rating})")
            println(movie.description)
        }
    }

    fun deleteMovie() {
        val file = File("movies.csv")
        val movies = getMoviesFromFile(file).toMutableList()
        if (movies.isEmpty()) {
            println("No movies found.")
            return
        }

        // Show numbered list
        println("Movie List")
        movies.forEachIndexed { index, movie ->
            println("${index + 1}. ${movie.name} (${movie.durationMinutes} min, Rating: ${movie.rating})")
        }

        var choice: Int
        do {
            print("Enter the number of the movie to delete: ")
            choice = readlnOrNull()?.toIntOrNull() ?: -1
            if (choice !in 1..movies.size - 1) {
                println("Invalid choice")
            }
        } while (choice !in 1..movies.size)

        val removedMovie = movies.removeAt(choice - 1)
        saveMoviesToFile(file, movies)
        println("Deleted movie: ${removedMovie.name}")
    }

    private fun getMoviesFromFile(file: File): List<Movie> {
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 5) {
                Movie(
                    name = parts[1],
                    durationMinutes = parts[3].toIntOrNull() ?: 0,
                    rating = parts[4],
                    description = parts[2]
                )
            } else null
        }
    }

    private fun saveMoviesToFile(file: File, movies: List<Movie>) {
        file.writeText("")
        movies.forEachIndexed { index, movie ->
            file.appendText("${index + 1},${movie.name},${movie.description},${movie.durationMinutes},${movie.rating}\n")
        }
    }
}