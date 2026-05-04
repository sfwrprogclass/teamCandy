package edu.teamcandy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.teamcandy.models.Auditorium
import edu.teamcandy.models.Theater
import edu.teamcandy.services.exposed.TheaterRepository

@Composable
fun TheatersScreen() {
    var theaters by remember { mutableStateOf(TheaterRepository.getAllTheaters()) }
    var showAddTheaterDialog by remember { mutableStateOf(false) }
    var showAddAuditoriumFor by remember { mutableStateOf<Theater?>(null) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Theaters & Auditoriums", style = MaterialTheme.typography.h5, modifier = Modifier.weight(1f))
            Button(onClick = { showAddTheaterDialog = true }) { Text("Add Theater") }
        }

        Divider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(theaters) { theater ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(theater.name, style = MaterialTheme.typography.subtitle1)
                                if (theater.location.isNotBlank())
                                    Text(theater.location, style = MaterialTheme.typography.body2)
                            }
                            OutlinedButton(onClick = { showAddAuditoriumFor = theater }) { Text("Add Auditorium") }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = {
                                TheaterRepository.deleteTheater(theater.id)
                                theaters = TheaterRepository.getAllTheaters()
                            }) { Text("Delete") }
                        }

                        theater.auditoriums.forEach { aud ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Auditorium ${aud.number}  |  ${aud.rows} rows × ${aud.seatsPerRow} seats",
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = {
                                    TheaterRepository.deleteAuditorium(aud.id)
                                    theaters = TheaterRepository.getAllTheaters()
                                }) { Text("Remove") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTheaterDialog) {
        AddTheaterDialog(
            onDismiss = { showAddTheaterDialog = false },
            onConfirm = { name, location ->
                TheaterRepository.addTheater(Theater(name = name, location = location))
                theaters = TheaterRepository.getAllTheaters()
                showAddTheaterDialog = false
            }
        )
    }

    showAddAuditoriumFor?.let { theater ->
        AddAuditoriumDialog(
            theaterName = theater.name,
            onDismiss = { showAddAuditoriumFor = null },
            onConfirm = { number, rows, seatsPerRow ->
                TheaterRepository.addAuditorium(Auditorium(number = number, theaterId = theater.id, rows = rows, seatsPerRow = seatsPerRow))
                theaters = TheaterRepository.getAllTheaters()
                showAddAuditoriumFor = null
            }
        )
    }
}

@Composable
fun AddTheaterDialog(onDismiss: () -> Unit, onConfirm: (name: String, location: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Theater") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, location) }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddAuditoriumDialog(theaterName: String, onDismiss: () -> Unit, onConfirm: (number: Int, rows: Int, seatsPerRow: Int) -> Unit) {
    var number by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf("5") }
    var seatsPerRow by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Auditorium to $theaterName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Auditorium Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rows, onValueChange = { rows = it }, label = { Text("Rows") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = seatsPerRow, onValueChange = { seatsPerRow = it }, label = { Text("Seats Per Row") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val n = number.toIntOrNull() ?: 0
                val r = rows.toIntOrNull() ?: 5
                val s = seatsPerRow.toIntOrNull() ?: 10
                if (n > 0) onConfirm(n, r, s)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}