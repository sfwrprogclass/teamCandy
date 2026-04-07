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
    // Rating of the movie
    var rating: String,
    // Description of the movie
    var description: String
)
