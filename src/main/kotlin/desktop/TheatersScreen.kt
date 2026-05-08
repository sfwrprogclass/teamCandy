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
    var banner by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                    val success = TheaterRepository.deleteTheater(theater.id)
                                    theaters = TheaterRepository.getAllTheaters()
                                    banner = if (success) true to "\"${theater.name}\" deleted."
                                             else false to "Failed to delete \"${theater.name}\"."
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
                                        val success = TheaterRepository.deleteAuditorium(aud.id)
                                        theaters = TheaterRepository.getAllTheaters()
                                        banner = if (success) true to "Auditorium ${aud.number} removed."
                                                 else false to "Failed to remove auditorium."
                                    }) { Text("Remove") }
                                }
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
    }

    if (showAddTheaterDialog) {
        AddTheaterDialog(
            onDismiss = { showAddTheaterDialog = false },
            onConfirm = { name, location ->
                try {
                    TheaterRepository.addTheater(Theater(name = name, location = location))
                    theaters = TheaterRepository.getAllTheaters()
                    showAddTheaterDialog = false
                    banner = true to "\"$name\" added successfully."
                } catch (e: Exception) {
                    showAddTheaterDialog = false
                    banner = false to "Failed to add theater."
                }
            }
        )
    }

    showAddAuditoriumFor?.let { theater ->
        AddAuditoriumDialog(
            theaterName = theater.name,
            onDismiss = { showAddAuditoriumFor = null },
            onConfirm = { number, rows, seatsPerRow ->
                try {
                    TheaterRepository.addAuditorium(Auditorium(number = number, theaterId = theater.id, rows = rows, seatsPerRow = seatsPerRow))
                    theaters = TheaterRepository.getAllTheaters()
                    showAddAuditoriumFor = null
                    banner = true to "Auditorium $number added to ${theater.name}."
                } catch (e: Exception) {
                    showAddAuditoriumFor = null
                    banner = false to "Failed to add auditorium."
                }
            }
        )
    }
}

@Composable
fun AddTheaterDialog(onDismiss: () -> Unit, onConfirm: (name: String, location: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Theater") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError
                )
                if (nameError) Text("Name is required.", color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { nameError = true; return@Button }
                onConfirm(name, location)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddAuditoriumDialog(theaterName: String, onDismiss: () -> Unit, onConfirm: (number: Int, rows: Int, seatsPerRow: Int) -> Unit) {
    var number by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf("5") }
    var seatsPerRow by remember { mutableStateOf("10") }
    var numberError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Auditorium to $theaterName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it; numberError = false },
                    label = { Text("Auditorium Number") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = numberError
                )
                if (numberError) Text("A valid auditorium number is required.", color = MaterialTheme.colors.error, style = MaterialTheme.typography.caption)
                OutlinedTextField(value = rows, onValueChange = { rows = it }, label = { Text("Rows") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = seatsPerRow, onValueChange = { seatsPerRow = it }, label = { Text("Seats Per Row") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val n = number.toIntOrNull()
                if (n == null || n <= 0) { numberError = true; return@Button }
                val r = rows.toIntOrNull() ?: 5
                val s = seatsPerRow.toIntOrNull() ?: 10
                onConfirm(n, r, s)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}