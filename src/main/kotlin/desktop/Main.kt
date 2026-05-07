package edu.teamcandy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import edu.teamcandy.services.exposed.init
import edu.teamcandy.services.exposed.startApiAndDatabase

fun main() {
    startApiAndDatabase()
    init()
    startComposeApp()
}

fun startComposeApp() = application {
    Window(onCloseRequest = ::exitApplication, title = "Candy Theaters — Admin") {
        MaterialTheme {
            AdminApp()
        }
    }
}

@Composable
fun AdminApp() {
    var selectedTab by remember { mutableStateOf(AdminTab.MOVIES) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            AdminTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                AdminTab.MOVIES -> MoviesScreen()
                AdminTab.THEATERS -> TheatersScreen()
                AdminTab.SHOWTIMES -> ShowtimesScreen()
                AdminTab.TICKETING -> TicketingScreen()
                AdminTab.REPORTS -> ReportsScreen()
            }
        }
    }
}

enum class AdminTab(val label: String) {
    MOVIES("Movies"),
    THEATERS("Theaters & Auditoriums"),
    SHOWTIMES("Showtimes"),
    TICKETING("Ticketing"),
    REPORTS("Reports")
}
