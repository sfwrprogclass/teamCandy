#!/usr/bin/env kotlin
package Mainframe.Maincode.Admin

import Mainframe.Maincode.Admin.Movie

fun showAdminMenu() {
    println("\n--- Admin Menu ---")
    println("1. Add a Movie")
    println("2. View All Movies")
    println("3. Back to Main Menu")
    print("Choose an option: ")

    when (readln().trim()) {
        "1" -> addMovieFlow()
        "2" -> listMoviesFlow()
        "3" -> return
        else -> println("Invalid option.")
    }
}

fun addMovieFlow() {
    println("\nAdding a new movie...")

    print("Enter movie title: ")
    val title = readln().trim()

    print("Enter duration (minutes): ")
    val duration = readln().trim().toIntOrNull() ?: run {
        println("Invalid duration.")
        return
    }

    print("Enter movie description: ")
    val description = readln().trim()

    print("Enter theater name/number where it is being showcased: ")
    val theater = readln().trim()

    val movie = Movie(title, duration, description, theater)
    MovieManager.addMovie(movie)

    println("\nMovie added successfully!")
}

fun listMoviesFlow() {
    val movies = MovieManager.listMovies()

    if (movies.isEmpty()) {
        println("\nNo movies added yet.")
        return
    }

    println("\n--- Movies ---")
    movies.forEachIndexed { index, movie ->
        println("${index + 1}. ${movie.title} (${movie.durationMinutes} min)")
        println("   Theater: ${movie.theater}")
        println("   Description: ${movie.description}")
    }
}
