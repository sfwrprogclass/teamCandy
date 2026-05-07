package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketResponse(
    val confirmationCode: String,
    val movieName: String,
    val startTime: String,
    val seats: List<String>,
    val totalPrice: Double,
    val qrCodeBase64: String
)