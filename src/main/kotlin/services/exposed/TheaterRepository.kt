package edu.teamcandy.services.exposed

import edu.teamcandy.exposed.AuditoriumTable
import edu.teamcandy.exposed.TheaterTable
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Theater
import edu.teamcandy.repository.TheaterRepositoryInterface
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object TheaterRepository : TheaterRepositoryInterface {

    override fun getAllTheaters(): List<Theater> = transaction {
        TheaterTable.selectAll().map {
            Theater(
                id = it[TheaterTable.id],
                name = it[TheaterTable.name],
                location = it[TheaterTable.location],
                auditoriums = getAuditoriumsByTheater(it[TheaterTable.id]).toMutableList()
            )
        }.toList()
    }

    override fun getTheaterById(id: Int): Theater? = transaction {
        TheaterTable.select { TheaterTable.id eq id }.map {
            Theater(
                id = it[TheaterTable.id],
                name = it[TheaterTable.name],
                location = it[TheaterTable.location],
                auditoriums = getAuditoriumsByTheater(it[TheaterTable.id]).toMutableList()
            )
        }.singleOrNull()
    }

    override fun addTheater(theater: Theater): Int = transaction {
        TheaterTable.insert {
            it[name] = theater.name
            it[location] = theater.location
        } get TheaterTable.id
    }

    override fun updateTheater(theater: Theater): Boolean = transaction {
        TheaterTable.update({ TheaterTable.id eq theater.id }) {
            it[name] = theater.name
            it[location] = theater.location
        } > 0
    }

    override fun deleteTheater(id: Int): Boolean = transaction {
        TheaterTable.deleteWhere { TheaterTable.id eq id } > 0
    }

    override fun getAuditoriumsByTheater(theaterId: Int): List<Auditorium> = transaction {
        AuditoriumTable.select { AuditoriumTable.theaterId eq theaterId }.map {
            Auditorium(
                id = it[AuditoriumTable.id],
                number = it[AuditoriumTable.number],
                theaterId = it[AuditoriumTable.theaterId],
                rows = it[AuditoriumTable.rows],
                seatsPerRow = it[AuditoriumTable.seatsPerRow]
            )
        }.toList()
    }

    override fun getTheaterByAuditoriums(auditoriumId: Int): Theater? = transaction {
        (AuditoriumTable innerJoin TheaterTable)
            .select { AuditoriumTable.id eq auditoriumId }
            .map {
                Theater(
                    id = it[TheaterTable.id],
                    name = it[TheaterTable.name],
                    location = it[TheaterTable.location]
                )
            }
            .firstOrNull()
    }

    override fun getAuditoriumById(id: Int): Auditorium? = transaction {
        AuditoriumTable.select { AuditoriumTable.id eq id }.map {
            Auditorium(
                id = it[AuditoriumTable.id],
                number = it[AuditoriumTable.number],
                theaterId = it[AuditoriumTable.theaterId],
                rows = it[AuditoriumTable.rows],
                seatsPerRow = it[AuditoriumTable.seatsPerRow]
            )
        }.singleOrNull() as Auditorium
    }

    override fun addAuditorium(auditorium: Auditorium): Int = transaction {
        AuditoriumTable.insert {
            it[number] = auditorium.number
            it[theaterId] = auditorium.theaterId
            it[rows] = auditorium.rows
            it[seatsPerRow] = auditorium.seatsPerRow
        } get AuditoriumTable.id
    }

    override fun updateAuditorium(auditorium: Auditorium): Boolean = transaction {
        AuditoriumTable.update({ AuditoriumTable.id eq auditorium.id }) {
            it[number] = auditorium.number
            it[theaterId] = auditorium.theaterId
            it[rows] = auditorium.rows
            it[seatsPerRow] = auditorium.seatsPerRow
        } > 0
    }

    override fun deleteAuditorium(id: Int): Boolean = transaction {
        AuditoriumTable.deleteWhere { AuditoriumTable.id eq id } > 0
    }
}
