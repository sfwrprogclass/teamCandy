package edu.teamcandy.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.teamcandy.models.TicketSalesReport
import edu.teamcandy.services.ReportService
import edu.teamcandy.services.exposed.TheaterRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen() {
    val reportService = remember { ReportService() }
    val theaters = remember { TheaterRepository.getAllTheaters() }
    
    var selectedTheaterId by remember { mutableStateOf(theaters.firstOrNull()?.id ?: 0) }
    var startDateText by remember { mutableStateOf(LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var endDateText by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    
    var report by remember { mutableStateOf<TicketSalesReport?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Generate Sales Report", style = MaterialTheme.typography.h5)
        
        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Theater:")
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(theaters.find { it.id == selectedTheaterId }?.name ?: "Select Theater")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            theaters.forEach { theater ->
                                DropdownMenuItem(onClick = {
                                    selectedTheaterId = theater.id
                                    expanded = false
                                }) {
                                    Text(theater.name)
                                }
                            }
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Button(
                    onClick = {
                        try {
                            val start = LocalDateTime.parse(startDateText + "T00:00:00")
                            val end = LocalDateTime.parse(endDateText + "T23:59:59")
                            report = reportService.generateTicketSalesReport(selectedTheaterId, start, end)
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            report = null
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Generate Report")
                }
            }
        }
        
        errorMessage?.let {
            Text(it, color = MaterialTheme.colors.error)
        }
        
        report?.let { r ->
            Divider()
            Text("Report for ${r.theaterName}", style = MaterialTheme.typography.h6)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Total Tickets Sold: ${r.totalTicketsSold}")
                Text("Total Revenue: $${"%.2f".format(r.totalRevenue)}")
            }
            
            Text("Sales by Movie:", style = MaterialTheme.typography.subtitle1)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(r.movieSales) { ms ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ms.movieName, modifier = Modifier.weight(1f))
                            Text("${ms.ticketsSold} sold", modifier = Modifier.width(80.dp))
                            Text("$${"%.2f".format(ms.revenue)}", modifier = Modifier.width(100.dp))
                        }
                    }
                }
            }
        }
    }
}
