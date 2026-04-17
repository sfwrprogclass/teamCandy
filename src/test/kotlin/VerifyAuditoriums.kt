
import edu.teamcandy.services.exposed.init
import edu.teamcandy.services.exposed.TheaterRepository

fun main() {
    init()
    val theaters = TheaterRepository.getAllTheaters()
    println("[DEBUG_LOG] Initial theaters: ${theaters.size}")
    theaters.forEach { theater ->
        val auds = TheaterRepository.getAuditoriumsByTheater(theater.id)
        println("[DEBUG_LOG] Theater: ${theater.name} (ID: ${theater.id}) has ${auds.size} auditoriums")
        auds.forEach { aud ->
            println("[DEBUG_LOG]   - Auditorium ${aud.number} (ID: ${aud.id})")
        }
    }
}
