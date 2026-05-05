package edu.teamcandy.models

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val nameOnCard: String,
    val cardNumber: String,
    val expiry: String,
    val cvv: String,
    val seats: List<Seat>
)