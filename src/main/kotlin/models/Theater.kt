package edu.teamcandy.models

data class Theater(
    val id: Int = 0,
    val name: String,
    val location: String = "",
    val auditoriums: MutableList<Auditorium> = mutableListOf()
)
