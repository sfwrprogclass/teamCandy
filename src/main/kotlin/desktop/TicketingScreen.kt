package edu.teamcandy.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.teamcandy.models.Showtime
import edu.teamcandy.models.Theater
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.TicketReceipt
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a")

@Composable
fun TicketingScreen() {
    val bookingService = remember { BookingService() }
    var theaters by remember { mutableStateOf(TheaterRepository.getAllTheaters()) }
    var selectedTheater by remember { mutableStateOf<Theater?>(null) }
    var selectedAuditorium by remember { mutableStateOf<Auditorium?>(null) }
    var showtimes by remember { mutableStateOf(listOf<Showtime>()) }
    var selectedShowtime by remember { mutableStateOf<Showtime?>(null) }
    var selectedSeats by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var statusMessage by remember { mutableStateOf("Please select a theater and auditorium to see showtimes.") }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var lastReceipt by remember { mutableStateOf<TicketReceipt?>(null) }
    var saleFailedMessage by remember { mutableStateOf("") }

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
                            selectedSeats = setOf()
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
                            selectedSeats = setOf()
                            showtimes = ShowtimeRepository.getAllShowtimes()
                                .filter { it.auditoriumId == aud.id }
                                .filter { !it.startTime.toLocalDate().isBefore(LocalDate.now()) }
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
                            selectedSeats = setOf()
                            statusMessage = "Selling tickets for ${showtime.movie.name}"
                        }) {
                            Text(
                                "${showtime.movie.name}\n${showtime.startTime.format(DISPLAY_FORMAT)}",
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
            
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.2f))) {
                selectedShowtime?.let { showtime ->
                    val rows = showtime.seatingChart.size
                    val cols = if (rows > 0) showtime.seatingChart[0].size else 0

                    if (rows > 0 && cols > 0) {
                        val padding = 16.dp
                        val spacing = 4.dp
                        val availableWidth = maxWidth - padding * 2
                        val availableHeight = maxHeight - padding * 2
                        val cellSize = minOf(
                            (availableWidth - spacing * (cols - 1)) / cols,
                            (availableHeight - spacing * (rows - 1)) / rows
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(spacing),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(padding).align(Alignment.Center)
                        ) {
                            repeat(rows) { r ->
                                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                                    repeat(cols) { c ->
                                        val seat = showtime.seatingChart[r][c]
                                        val isSelected = selectedSeats.contains(r to c)
                                        Button(
                                            onClick = {
                                                selectedSeats = if (isSelected) {
                                                    selectedSeats - (r to c)
                                                } else {
                                                    selectedSeats + (r to c)
                                                }
                                            },
                                            enabled = !seat.isReserved,
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = when {
                                                    seat.isReserved -> Color.Red
                                                    isSelected -> Color.Yellow
                                                    else -> Color.Green
                                                },
                                                disabledBackgroundColor = Color.Red
                                            ),
                                            modifier = Modifier.size(cellSize),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("${'A' + r}${c + 1}", style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: Text("No showtime selected", modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(statusMessage, modifier = Modifier.weight(1f))
                
                if (selectedSeats.isNotEmpty()) {
                    Button(onClick = { showConfirmationDialog = true }) {
                        Text("Confirm Selection (${selectedSeats.size})")
                    }
                }

                Button(onClick = {
                    theaters = TheaterRepository.getAllTheaters()
                    selectedShowtime?.let { current ->
                        selectedShowtime = ShowtimeRepository.getAllShowtimes().find { it.id == current.id }
                    }
                    selectedSeats = setOf()
                }) {
                    Text("Refresh")
                }
            }
        }
    }

    if (showConfirmationDialog && selectedShowtime != null) {
        val showtime = selectedShowtime!!
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Purchase") },
            text = {
                Column {
                    Text("Movie: ${showtime.movie.name}")
                    Text("Time: ${showtime.startTime.format(DISPLAY_FORMAT)}")
                    Text("Seats: ${selectedSeats.joinToString(", ") { (r, c) -> "${'A' + r}${c + 1}" }}")
                    Text("Total Price: $${"%.2f".format(selectedSeats.size * showtime.unitPrice)}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmationDialog = false
                    val receipt = bookingService.sellTickets(showtime, selectedSeats.toList())
                    lastReceipt = receipt
                    if (receipt != null) {
                        selectedShowtime = ShowtimeRepository.getAllShowtimes().find { it.id == showtime.id }
                        selectedSeats = setOf()
                    } else {
                        saleFailedMessage = "Failed to sell tickets. One or more seats are already reserved."
                    }
                    showResultDialog = true
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResultDialog) {
        val receipt = lastReceipt
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = null,
            text = {
                if (receipt != null) {
                    val qrImage = remember(receipt.confirmationCode) {
                        generateQrImage(receipt.confirmationCode, size = 200)
                    }
                    Column(
                        modifier = Modifier.width(380.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Header
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colors.primary)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "CANDY CINEMA",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, letterSpacing = 3.sp),
                                color = Color.White
                            )
                        }

                        // Ticket body
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(receipt.movieName, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
                            Text(receipt.startTime, style = MaterialTheme.typography.body2)
                            Text("Seats: ${receipt.seatNames.joinToString(", ")}", style = MaterialTheme.typography.body2)
                            Text("Total: $${"%.2f".format(receipt.totalPrice)}", style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold))
                        }

                        // Tear line
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(32) {
                                Text("- ", style = MaterialTheme.typography.caption, color = Color.LightGray)
                            }
                        }

                        // QR + code section
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = qrImage.toComposeImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(160.dp)
                            )
                            Text(
                                receipt.confirmationCode,
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colors.primary
                            )
                            Text(
                                "Present at the theater entrance",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Text(saleFailedMessage)
                }
            },
            confirmButton = {
                val receipt = lastReceipt
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (receipt != null) {
                        Button(onClick = {
                            val qrImage = generateQrImage(receipt.confirmationCode, size = 400)
                            printTicket(receipt, qrImage)
                        }) {
                            Text("Print Ticket")
                        }
                    }
                    OutlinedButton(onClick = { showResultDialog = false }) {
                        Text("Done")
                    }
                }
            },
            dismissButton = null
        )
    }
}
