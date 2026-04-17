package edu.teamcandy.utils

import java.time.format.DateTimeFormatter

object Constants {
    val SHOWTIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")
    val INPUT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
}
