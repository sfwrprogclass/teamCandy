package edu.teamcandy.ui

import edu.teamcandy.models.Seat
import edu.teamcandy.models.Showtime
import edu.teamcandy.services.BookingService
import edu.teamcandy.services.exposed.ShowtimeRepository
import edu.teamcandy.services.exposed.TheaterRepository
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class EmployeeTicketSaleGUI : JFrame("Candy Theaters - Employee Ticket Sale") {
    private val bookingService = BookingService()
    
    private val theaterComboBox = JComboBox<String>()
    private val auditoriumComboBox = JComboBox<String>()
    private val showtimeListModel = DefaultListModel<String>()
    private val showtimeList = JList(showtimeListModel)
    
    private val seatingPanel = JPanel()
    private val statusLabel = JLabel("Please select a theater and auditorium to see showtimes.")
    
    private var currentShowtimes = listOf<Showtime>()
    private var selectedShowtime: Showtime? = null

    init {
        size = Dimension(900, 600)
        layout = BorderLayout()
        defaultCloseOperation = DISPOSE_ON_CLOSE

        val leftPanel = JPanel()
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        leftPanel.preferredSize = Dimension(300, 600)
        leftPanel.border = EmptyBorder(10, 10, 10, 10)

        // Theater Selection
        leftPanel.add(JLabel("Select Theater:"))
        loadTheaters()
        theaterComboBox.addActionListener {
            loadAuditoriums()
        }
        leftPanel.add(theaterComboBox)
        leftPanel.add(Box.createRigidArea(Dimension(0, 10)))

        // Auditorium Selection
        leftPanel.add(JLabel("Select Auditorium:"))
        auditoriumComboBox.addActionListener {
            loadShowtimes()
        }
        leftPanel.add(auditoriumComboBox)
        leftPanel.add(Box.createRigidArea(Dimension(0, 10)))

        // Showtime Selection
        leftPanel.add(JLabel("Select Showtime:"))
        showtimeList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        showtimeList.addListSelectionListener {
            val index = showtimeList.selectedIndex
            if (index >= 0 && index < currentShowtimes.size) {
                displaySeatingChart(currentShowtimes[index])
            }
        }
        val scrollPane = JScrollPane(showtimeList)
        leftPanel.add(scrollPane)

        add(leftPanel, BorderLayout.WEST)

        // Seating Chart Area
        val rightPanel = JPanel(BorderLayout())
        rightPanel.border = TitledBorder("Seating Chart")
        
        seatingPanel.layout = GridLayout(0, 1) // Will be updated
        rightPanel.add(JScrollPane(seatingPanel), BorderLayout.CENTER)
        
        val bottomPanel = JPanel(BorderLayout())
        bottomPanel.add(statusLabel, BorderLayout.CENTER)
        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { loadShowtimes() }
        bottomPanel.add(refreshButton, BorderLayout.EAST)
        rightPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(rightPanel, BorderLayout.CENTER)

        loadAuditoriums() // Initial load
    }

    private fun loadTheaters() {
        theaterComboBox.removeAllItems()
        val theaters = TheaterRepository.getAllTheaters()
        theaters.forEach { theaterComboBox.addItem(it.name) }
    }

    private fun loadAuditoriums() {
        auditoriumComboBox.removeAllItems()
        val theaterName = theaterComboBox.selectedItem as? String ?: return
        val theater = TheaterRepository.getAllTheaters().find { it.name == theaterName } ?: return
        
        theater.auditoriums.forEach { auditoriumComboBox.addItem("Auditorium ${it.number}") }
    }

    private fun loadShowtimes() {
        val selectedId = selectedShowtime?.id
        
        showtimeListModel.clear()
        seatingPanel.removeAll()
        seatingPanel.revalidate()
        seatingPanel.repaint()
        // Don't null out selectedShowtime immediately if we want to try to restore it
        
        val theaterName = theaterComboBox.selectedItem as? String ?: return
        val audName = auditoriumComboBox.selectedItem as? String ?: return
        val audNum = audName.removePrefix("Auditorium ").toIntOrNull() ?: return
        
        val theater = TheaterRepository.getAllTheaters().find { it.name == theaterName } ?: return
        val auditorium = theater.auditoriums.find { it.number == audNum } ?: return
        
        // Ensure showtimes are loaded for this auditorium
        currentShowtimes = ShowtimeRepository.getAllShowtimes().filter { it.auditoriumId == auditorium.id }
        
        if (currentShowtimes.isEmpty()) {
            selectedShowtime = null
            statusLabel.text = "No showtimes found for this auditorium."
        } else {
            var selectedIndex = -1
            currentShowtimes.forEachIndexed { index, it ->
                showtimeListModel.addElement("${it.movie.name} - ${it.startTime}")
                if (it.id == selectedId) {
                    selectedIndex = index
                }
            }
            
            if (selectedIndex != -1) {
                showtimeList.selectedIndex = selectedIndex
                displaySeatingChart(currentShowtimes[selectedIndex])
            } else {
                selectedShowtime = null
                statusLabel.text = "Select a showtime to view seating."
            }
        }
    }

    private fun displaySeatingChart(showtime: Showtime) {
        selectedShowtime = showtime
        seatingPanel.removeAll()
        
        val rows = showtime.seatingChart.size
        val cols = if (rows > 0) showtime.seatingChart[0].size else 0
        
        seatingPanel.layout = GridLayout(rows, cols, 5, 5)
        
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val seat = showtime.seatingChart[r][c]
                val seatButton = JButton("${'A' + r}${c + 1}")
                if (seat.isReserved) {
                    seatButton.background = Color.RED
                    seatButton.isEnabled = false
                } else {
                    seatButton.background = Color.GREEN
                    seatButton.addActionListener {
                        sellTicket(showtime, r, c, seatButton)
                    }
                }
                seatingPanel.add(seatButton)
            }
        }
        
        seatingPanel.revalidate()
        seatingPanel.repaint()
        statusLabel.text = "Selling tickets for ${showtime.movie.name}"
    }

    private fun sellTicket(showtime: Showtime, row: Int, col: Int, button: JButton) {
        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Sell ticket for seat ${'A' + row}${col + 1}?",
            "Confirm Sale",
            JOptionPane.YES_NO_OPTION
        )
        
        if (confirm == JOptionPane.YES_OPTION) {
            val result = bookingService.sellTicket(showtime, row, col)
            if (result.contains("successfully")) {
                button.background = Color.RED
                button.isEnabled = false
                JOptionPane.showMessageDialog(this, result)
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }
}

fun main() {
    // For testing standalone
    edu.teamcandy.services.exposed.init()
    SwingUtilities.invokeLater {
        val gui = EmployeeTicketSaleGUI()
        gui.isVisible = true
    }
}
