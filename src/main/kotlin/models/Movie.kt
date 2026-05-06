package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int = 0,
    var name: String,
    var durationMinutes: Int,
    var rating: String,
    var description: String,
    var imageUrl: String
) {
    // Secondary constructor for tests
    constructor(name: String, durationMinutes: Int) : this(
        id = 0,
        name = name,
        durationMinutes = durationMinutes,
        rating = "NR",
        description = "",
        imageUrl = ""
    )
}
