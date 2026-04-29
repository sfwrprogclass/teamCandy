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
import edu.teamcandy.services.exposed.MovieRepository

@Composable
fun MoviesScreen() {
    var movies by remember { mutableStateOf(MovieRepository.getAllMovies()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMovie by remember { mutableStateOf<Movie?>(null) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Movies", style = MaterialTheme.typography.h5, modifier = Modifier.weight(1f))
            Button(onClick = { showAddDialog = true }) { Text("Add Movie") }
        }

        Divider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(movies) { movie ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.name, style = MaterialTheme.typography.subtitle1)
                            Text("${movie.durationMinutes} min  |  Rating: ${movie.rating}", style = MaterialTheme.typography.body2)
                            if (movie.description.isNotBlank())
                                Text(movie.description, style = MaterialTheme.typography.caption)
                        }
                        OutlinedButton(onClick = { editingMovie = movie }) { Text("Edit") }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = {
                            MovieRepository.deleteMovie(movie.id)
                            movies = MovieRepository.getAllMovies()
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMovieDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, duration, rating, description ->
                MovieRepository.addMovie(Movie(name = name, durationMinutes = duration, rating = rating, description = description))
                movies = MovieRepository.getAllMovies()
                showAddDialog = false
            }
        )
    }

    editingMovie?.let { movie ->
        EditMovieDialog(
            movie = movie,
            onDismiss = { editingMovie = null },
            onConfirm = { name, duration, rating, description ->
                MovieRepository.updateMovie(movie.id, movie.copy(name = name, durationMinutes = duration, rating = rating, description = description))
                movies = MovieRepository.getAllMovies()
                editingMovie = null
            }
        )
    }
}

@Composable
fun AddMovieDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, duration: Int, rating: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("PG") }
    var description by remember { mutableStateOf("") }
    var ratingExpanded by remember { mutableStateOf(false) }
    val ratings = listOf("G", "PG", "PG-13", "R", "NC-17")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Movie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { ratingExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Rating: $rating")
                    }
                    DropdownMenu(expanded = ratingExpanded, onDismissRequest = { ratingExpanded = false }) {
                        ratings.forEach { r ->
                            DropdownMenuItem(onClick = { rating = r; ratingExpanded = false }) {
                                Text(r)
                            }
                        }
                    }
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val dur = duration.toIntOrNull() ?: 0
                if (name.isNotBlank()) onConfirm(name, dur, rating, description)
            }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditMovieDialog(
    movie: Movie,
    onDismiss: () -> Unit,
    onConfirm: (name: String, duration: Int, rating: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(movie.name) }
    var duration by remember { mutableStateOf(movie.durationMinutes.toString()) }
    var rating by remember { mutableStateOf(movie.rating.ifBlank { "PG" }) }
    var description by remember { mutableStateOf(movie.description) }
    var ratingExpanded by remember { mutableStateOf(false) }
    val ratings = listOf("G", "PG", "PG-13", "R", "NC-17")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Movie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { ratingExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Rating: $rating")
                    }
                    DropdownMenu(expanded = ratingExpanded, onDismissRequest = { ratingExpanded = false }) {
                        ratings.forEach { r ->
                            DropdownMenuItem(onClick = { rating = r; ratingExpanded = false }) {
                                Text(r)
                            }
                        }
                    }
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val dur = duration.toIntOrNull() ?: 0
                if (name.isNotBlank()) onConfirm(name, dur, rating, description)
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}