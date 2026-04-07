package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    // Identifier
    val id: Int = 0,
    // Title of the movie
    var name: String,
    // The duration of the movie
    var durationMinutes: Int,
    // The rating of the movie
    val rating: String = "",
    // The cast members
    val cast: List<String> = emptyList(),
    // The genres
    val genres: List<String> = emptyList(),
    // Movie description
    var description: String = ""
)
