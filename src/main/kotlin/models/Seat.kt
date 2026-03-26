package edu.teamcandy.models

data class Seat(
    val row: Int,
    val number: Int,
    var isReserved: Boolean = false
) {
    override fun toString(): String {
        return "${'A' + row}${number + 1}"
    }
}
