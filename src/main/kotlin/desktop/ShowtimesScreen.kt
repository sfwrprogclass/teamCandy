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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")

@Composable
fun ShowtimesScreen() {
    var showtimes by remember { mutableStateOf(ShowtimeRepository.getAllShowtimes()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Showtimes", style = MaterialTheme.typography.h5, modifier = Modifier.weight(1f))
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
                                Text("Auditorium ID: ${showtime.auditoriumId}", style = MaterialTheme.typography.caption)
                            }
                            OutlinedButton(onClick = {
                                ShowtimeRepository.deleteShowtime(showtime.id)
                                showtimes = ShowtimeRepository.getAllShowtimes()
                            }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddShowtimeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { showtime ->
                ShowtimeRepository.addShowtime(showtime)
                showtimes = ShowtimeRepository.getAllShowtimes()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddShowtimeDialog(onDismiss: () -> Unit, onConfirm: (Showtime) -> Unit) {
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
                onConfirm(showtime)
            }) { Text("Schedule") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
