package edu.teamcandy.models

data class TicketReceipt(
    val confirmationCode: String,
    val movieName: String,
    val startTime: String,
    val seatNames: List<String>,
    val totalPrice: Double
)