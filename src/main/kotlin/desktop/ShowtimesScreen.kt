package edu.teamcandy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.teamcandy.models.Movie
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.exposed.MovieRepository
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")

@Composable
fun ShowtimesScreen() {
    var allShowtimes by remember { mutableStateOf(ShowtimeRepository.getAllShowtimes()) }
    val theaters = remember { TheaterRepository.getAllTheaters() }
    val auditoriumLabels = remember(theaters) {
        theaters.flatMap { t -> t.auditoriums.map { a -> a.id to "${t.name} — Aud. ${a.number}" } }.toMap()
    }
    var showUpcomingOnly by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var banner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val showtimes = if (showUpcomingOnly)
        allShowtimes.filter { !it.startTime.toLocalDate().isBefore(LocalDate.now()) }
    else
        allShowtimes

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Showtimes", style = MaterialTheme.typography.h5, modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { showUpcomingOnly = true },
                colors = if (showUpcomingOnly)
                    ButtonDefaults.outlinedButtonColors(backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.15f))
                else
                    ButtonDefaults.outlinedButtonColors()
            ) { Text("Upcoming") }
            OutlinedButton(
                onClick = { showUpcomingOnly = false },
                colors = if (!showUpcomingOnly)
                    ButtonDefaults.outlinedButtonColors(backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.15f))
                else
                    ButtonDefaults.outlinedButtonColors()
            ) { Text("All") }
            Button(onClick = { showAddDialog = true }) { Text("Schedule Showtime") }
        }

        Divider()

        if (showtimes.isEmpty()) {
            Text("No showtimes scheduled.", style = MaterialTheme.typography.body1)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(showtimes) { showtime ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(showtime.movie.name, style = MaterialTheme.typography.subtitle1)
                                Text(showtime.startTime.format(DISPLAY_FORMAT), style = MaterialTheme.typography.body2)
                                Text(auditoriumLabels[showtime.auditoriumId] ?: "Auditorium ${showtime.auditoriumId}", style = MaterialTheme.typography.caption)
                            }
                            OutlinedButton(onClick = {
                                val success = ShowtimeRepository.deleteShowtime(showtime.id)
                                allShowtimes = ShowtimeRepository.getAllShowtimes()
                                banner = if (success) true to "Showtime removed."
                                         else false to "Failed to remove showtime."
                            }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }

    banner?.let { (isSuccess, msg) ->
        StatusBanner(
            message = msg,
            isSuccess = isSuccess,
            onDismiss = { banner = null },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
} // closes Box

    if (showAddDialog) {
        AddShowtimeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { showtime ->
                val id = ShowtimeRepository.addShowtime(showtime)
                if (id != null) {
                    allShowtimes = ShowtimeRepository.getAllShowtimes()
                    showAddDialog = false
                    banner = true to "Showtime scheduled for ${showtime.movie.name}."
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
fun AddShowtimeDialog(onDismiss: () -> Unit, onConfirm: (Showtime) -> Boolean) {
    val movies = remember { MovieRepository.getAllMovies() }
    val theaters = remember { TheaterRepository.getAllTheaters() }

    val now = remember { java.time.LocalDateTime.now() }

    var selectedMovie by remember { mutableStateOf<Movie?>(movies.firstOrNull()) }
    var selectedAuditoriumId by remember { mutableStateOf<Int?>(null) }
    var movieExpanded by remember { mutableStateOf(false) }
    var theaterExpanded by remember { mutableStateOf(false) }

    var selectedMonth by remember { mutableStateOf(now.monthValue) }
    var selectedDay by remember { mutableStateOf(now.dayOfMonth) }
    var selectedYear by remember { mutableStateOf(now.year) }
    var selectedHour by remember { mutableStateOf(if (now.hour % 12 == 0) 12 else now.hour % 12) }
    var selectedMinute by remember { mutableStateOf((now.minute / 5) * 5) }
    var selectedAmPm by remember { mutableStateOf(if (now.hour < 12) "AM" else "PM") }
    var monthExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }
    var amPmExpanded by remember { mutableStateOf(false) }
    var scheduleError by remember { mutableStateOf<String?>(null) }

    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val daysInMonth = java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()

    val allAuditoriums = theaters.flatMap { t -> t.auditoriums.map { a -> t.name to a } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Showtime") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Movie picker
                Column {
                    Text("Movie", style = MaterialTheme.typography.caption)
                    Box {
                        OutlinedButton(onClick = { movieExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedMovie?.name ?: "Select Movie")
                        }
                        DropdownMenu(expanded = movieExpanded, onDismissRequest = { movieExpanded = false }) {
                            movies.forEach { movie ->
                                DropdownMenuItem(onClick = { selectedMovie = movie; movieExpanded = false }) {
                                    Text(movie.name)
                                }
                            }
                        }
                    }
                }

                // Auditorium picker
                Column {
                    Text("Auditorium", style = MaterialTheme.typography.caption)
                    Box {
                        OutlinedButton(onClick = { theaterExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            val label = allAuditoriums.firstOrNull { it.second.id == selectedAuditoriumId }
                                ?.let { "${it.first} — Aud. ${it.second.number}" } ?: "Select Auditorium"
                            Text(label)
                        }
                        DropdownMenu(expanded = theaterExpanded, onDismissRequest = { theaterExpanded = false }) {
                            allAuditoriums.forEach { (theaterName, aud) ->
                                DropdownMenuItem(onClick = { selectedAuditoriumId = aud.id; theaterExpanded = false }) {
                                    Text("$theaterName — Aud. ${aud.number}")
                                }
                            }
                        }
                    }
                }

                Column {
                    Text("Date", style = MaterialTheme.typography.caption)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Month
                        Box(modifier = Modifier.weight(2f)) {
                            OutlinedButton(onClick = { monthExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(months[selectedMonth - 1])
                            }
                            DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                months.forEachIndexed { i, m ->
                                    DropdownMenuItem(onClick = {
                                        selectedMonth = i + 1
                                        if (selectedDay > java.time.YearMonth.of(selectedYear, i + 1).lengthOfMonth())
                                            selectedDay = java.time.YearMonth.of(selectedYear, i + 1).lengthOfMonth()
                                        monthExpanded = false
                                    }) { Text(m) }
                                }
                            }
                        }
                        // Day
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { dayExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedDay.toString())
                            }
                            DropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                                (1..daysInMonth).forEach { d ->
                                    DropdownMenuItem(onClick = { selectedDay = d; dayExpanded = false }) { Text(d.toString()) }
                                }
                            }
                        }
                        // Year
                        Box(modifier = Modifier.weight(2f)) {
                            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedYear.toString())
                            }
                        }
                    }
                }

                Column {
                    Text("Time", style = MaterialTheme.typography.caption)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Hour
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { hourExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedHour.toString())
                            }
                            DropdownMenu(expanded = hourExpanded, onDismissRequest = { hourExpanded = false }) {
                                (1..12).forEach { h ->
                                    DropdownMenuItem(onClick = { selectedHour = h; hourExpanded = false }) { Text(h.toString()) }
                                }
                            }
                        }
                        Text(":", style = MaterialTheme.typography.h6)
                        // Minute
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { minuteExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedMinute.toString().padStart(2, '0'))
                            }
                            DropdownMenu(expanded = minuteExpanded, onDismissRequest = { minuteExpanded = false }) {
                                (0..55 step 5).forEach { m ->
                                    DropdownMenuItem(onClick = { selectedMinute = m; minuteExpanded = false }) {
                                        Text(m.toString().padStart(2, '0'))
                                    }
                                }
                            }
                        }
                        // AM/PM
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { amPmExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedAmPm)
                            }
                            DropdownMenu(expanded = amPmExpanded, onDismissRequest = { amPmExpanded = false }) {
                                listOf("AM", "PM").forEach { ap ->
                                    DropdownMenuItem(onClick = { selectedAmPm = ap; amPmExpanded = false }) { Text(ap) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                scheduleError?.let {
                    Text(it, color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Button(onClick = {
                    val movie = selectedMovie ?: return@Button
                    val audId = selectedAuditoriumId ?: return@Button
                    val hour24 = when {
                        selectedAmPm == "AM" && selectedHour == 12 -> 0
                        selectedAmPm == "PM" && selectedHour != 12 -> selectedHour + 12
                        else -> selectedHour
                    }
                    val time = LocalDateTime.of(selectedYear, selectedMonth, selectedDay, hour24, selectedMinute)
                    val aud = allAuditoriums.first { it.second.id == audId }.second
                    val showtime = Showtime(
                        movie = movie,
                        startTime = time,
                        auditoriumId = audId,
                        seatingChart = List(aud.rows) { r -> List(aud.seatsPerRow) { c -> edu.teamcandy.models.Seat(r, c) } }
                    )
                    val success = onConfirm(showtime)
                    if (!success) scheduleError = "This auditorium already has a showtime during that time."
                }) { Text("Schedule") }
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
