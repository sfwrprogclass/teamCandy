package edu.teamcandy.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TicketSalesReport(
    val theaterName: String,
    val startDate: @Serializable(with = LocalDateTimeSerializer::class) LocalDateTime,
    val endDate: @Serializable(with = LocalDateTimeSerializer::class) LocalDateTime,
    val totalTicketsSold: Int,
    val totalRevenue: Double,
    val movieSales: List<MovieSalesReport>
)

@Serializable
data class MovieSalesReport(
    val movieName: String,
    val ticketsSold: Int,
    val revenue: Double
)
