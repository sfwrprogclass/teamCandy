package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Theater(
    val id: Int = 0,
    val name: String,
    val location: String = "",
    val auditoriums: MutableList<Auditorium> = mutableListOf()
)
