package edu.teamcandy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import edu.teamcandy.models.Auditorium
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository

@Composable
fun TicketingScreen() {
    val bookingService = remember { BookingService() }
    var theaters by remember { mutableStateOf(TheaterRepository.getAllTheaters()) }
    var selectedTheater by remember { mutableStateOf<Theater?>(null) }
    var selectedAuditorium by remember { mutableStateOf<Auditorium?>(null) }
    var showtimes by remember { mutableStateOf(listOf<Showtime>()) }
    var selectedShowtime by remember { mutableStateOf<Showtime?>(null) }
    var statusMessage by remember { mutableStateOf("Please select a theater and auditorium to see showtimes.") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel - Selection
        Column(modifier = Modifier.width(300.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Select Theater", style = MaterialTheme.typography.subtitle1)
            var theaterExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { theaterExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedTheater?.name ?: "Choose Theater")
                }
                DropdownMenu(expanded = theaterExpanded, onDismissRequest = { theaterExpanded = false }) {
                    theaters.forEach { theater ->
                        DropdownMenuItem(onClick = {
                            selectedTheater = theater
                            selectedAuditorium = null
                            selectedShowtime = null
                            showtimes = listOf()
                            theaterExpanded = false
                        }) {
                            Text(theater.name)
                        }
                    }
                }
            }

            Text("Select Auditorium", style = MaterialTheme.typography.subtitle1)
            var audExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { audExpanded = true }, modifier = Modifier.fillMaxWidth(), enabled = selectedTheater != null) {
                    Text(selectedAuditorium?.let { "Auditorium ${it.number}" } ?: "Choose Auditorium")
                }
                DropdownMenu(expanded = audExpanded, onDismissRequest = { audExpanded = false }) {
                    selectedTheater?.auditoriums?.forEach { aud ->
                        DropdownMenuItem(onClick = {
                            selectedAuditorium = aud
                            selectedShowtime = null
                            showtimes = ShowtimeRepository.getAllShowtimes().filter { it.auditoriumId == aud.id }
                            audExpanded = false
                            if (showtimes.isEmpty()) statusMessage = "No showtimes found for this auditorium."
                            else statusMessage = "Select a showtime to view seating."
                        }) {
                            Text("Auditorium ${aud.number}")
                        }
                    }
                }
            }

            Text("Select Showtime", style = MaterialTheme.typography.subtitle1)
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(showtimes) { showtime ->
                    val isSelected = selectedShowtime?.id == showtime.id
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = if (isSelected) 8.dp else 2.dp,
                        backgroundColor = if (isSelected) MaterialTheme.colors.primarySurface else MaterialTheme.colors.surface
                    ) {
                        TextButton(onClick = {
                            selectedShowtime = showtime
                            statusMessage = "Selling tickets for ${showtime.movie.name}"
                        }) {
                            Text(
                                "${showtime.movie.name}\n${showtime.startTime}",
                                color = if (isSelected) contentColorFor(MaterialTheme.colors.primarySurface) else contentColorFor(MaterialTheme.colors.surface)
                            )
                        }
                    }
                }
            }
        }

        // Right Panel - Seating Chart
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
            Text("Seating Chart", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.2f)).padding(16.dp)) {
                selectedShowtime?.let { showtime ->
                    val rows = showtime.seatingChart.size
                    val cols = if (rows > 0) showtime.seatingChart[0].size else 0
                    
                    if (rows > 0 && cols > 0) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(cols),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(rows * cols) { index ->
                                val r = index / cols
                                val c = index % cols
                                val seat = showtime.seatingChart[r][c]
                                
                                Button(
                                    onClick = {
                                        val result = bookingService.sellTicket(showtime, r, c)
                                        if (result.contains("successfully")) {
                                            // Refresh showtime data
                                            selectedShowtime = ShowtimeRepository.getAllShowtimes().find { it.id == showtime.id }
                                        }
                                        statusMessage = result
                                    },
                                    enabled = !seat.isReserved,
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (seat.isReserved) Color.Red else Color.Green,
                                        disabledBackgroundColor = Color.Red
                                    ),
                                    modifier = Modifier.aspectRatio(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("${'A' + r}${c + 1}", style = MaterialTheme.typography.caption, color = Color.White)
                                }
                            }
                        }
                    }
                } ?: Text("No showtime selected", modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(statusMessage, modifier = Modifier.weight(1f))
                Button(onClick = {
                    theaters = TheaterRepository.getAllTheaters()
                    selectedShowtime?.let {
                        selectedShowtime = ShowtimeRepository.getAllShowtimes().find { it.id == it.id }
                    }
                }) {
                    Text("Refresh")
                }
            }
        }
    }
}
