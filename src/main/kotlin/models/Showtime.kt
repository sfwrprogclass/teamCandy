package edu.teamcandy.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }
}

@Serializable
data class Showtime(
    // Identifier
    val id: Int = 0,
    // Association movie object
    val movie: Movie,
    // When the trailers/previews begin
    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime,
    // Auditorium ID where the show is playing
    val auditoriumId: Int,
    // Seating chart for this specific showtime
    val seatingChart: List<List<Seat>>,
    // Time needed for trailers, to determine the actual end time
    // when taking the movies duration into consideration
    val paddingMinutes: Int = 15,
    // Unit price of Showtime
    val unitPrice: Double = 12.00,
) {
    // Calculate end time from the movies star time, adding
    // trailer padding and movie duration
    val endTime: LocalDateTime
        get() = startTime.plusMinutes((movie.durationMinutes + paddingMinutes).toLong())

    fun isSeatAvailable(row: Int, number: Int): Boolean {
        return row in seatingChart.indices &&
                number in seatingChart[row].indices &&
                !seatingChart[row][number].isReserved
    }

    fun reserveSeat(row: Int, number: Int): Boolean {
        if (isSeatAvailable(row, number)) {
            seatingChart[row][number].isReserved = true
            return true
        }
        return false
    }
}