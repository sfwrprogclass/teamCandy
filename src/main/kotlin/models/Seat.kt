package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Seat(
    val row: Int,
    val number: Int,
    var isReserved: Boolean = false
) {
    override fun toString(): String {
        return "${'A' + row}${number + 1}"
    }
}
