package edu.teamcandy.models

data class Movie(
    // Title of the movie
    val name: String,
    // The duration of the movie
    val durationMinutes: Int,
    // The cast members
    val cast: List<String> = emptyList(),
    // The genres
    val genres: List<String> = emptyList(),
    // Movie description
    val description: String = ""
)