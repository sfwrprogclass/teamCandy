package edu.teamcandy.repository

import edu.teamcandy.models.Theater
import edu.teamcandy.models.Auditorium

interface TheaterRepositoryInterface {
    fun getAllTheaters(): List<Theater>
    fun getTheaterById(id: Int): Theater?
    fun addTheater(theater: Theater): Int
    fun updateTheater(theater: Theater): Boolean
    fun deleteTheater(id: Int): Boolean
    
    fun getAuditoriumsByTheater(theaterId: Int): List<Auditorium>
    fun getAuditoriumById(theaterId: Int): Auditorium?
    fun getTheaterByAuditoriums(auditoriumId: Int): Theater?
    fun addAuditorium(auditorium: Auditorium): Int
    fun updateAuditorium(auditorium: Auditorium): Boolean
    fun deleteAuditorium(id: Int): Boolean
}
