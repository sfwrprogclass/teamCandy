package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    // Identifier
    val id: Int = 0,
    // Title of the movie
    var name: String,
    // The duration of the movie
    val durationMinutes: Int,
    // The cast members
    val cast: List<String> = emptyList(),
    // The genres
    val genres: List<String> = emptyList(),
    // Movie description
    val description: String = ""
)
