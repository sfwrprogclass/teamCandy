package edu.teamcandy.services

import edu.teamcandy.models.MovieSalesReport
import edu.teamcandy.models.TicketSalesReport
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import java.time.LocalDateTime

class ReportService {
    fun generateTicketSalesReport(theaterId: Int, startDate: LocalDateTime, endDate: LocalDateTime): TicketSalesReport {
        val theater = TheaterRepository.getTheaterById(theaterId) ?: throw IllegalArgumentException("Theater not found")
        val salesData = ShowtimeRepository.getTicketsSoldByTheaterAndDateRange(theaterId, startDate, endDate)

        val totalTicketsSold = salesData.sumOf { it.second }
        val totalRevenue = salesData.sumOf { it.first.unitPrice * it.second }

        val movieSales = salesData.groupBy { it.first.movie.id }.map { (movieId, data) ->
            val movieName = data.first().first.movie.name
            val ticketsSold = data.sumOf { it.second }
            val revenue = data.sumOf { it.first.unitPrice * it.second }
            MovieSalesReport(movieName, ticketsSold, revenue)
        }

        return TicketSalesReport(
            theaterName = theater.name,
            startDate = startDate,
            endDate = endDate,
            totalTicketsSold = totalTicketsSold,
            totalRevenue = totalRevenue,
            movieSales = movieSales
        )
    }
}
