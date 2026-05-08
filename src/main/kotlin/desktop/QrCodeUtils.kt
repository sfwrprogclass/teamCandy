package edu.teamcandy.desktop

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import edu.teamcandy.models.TicketReceipt
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterJob

fun generateQrImage(content: String, size: Int = 200): BufferedImage {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    return MatrixToImageWriter.toBufferedImage(bitMatrix)
}

fun printTicket(receipt: TicketReceipt, qrImage: BufferedImage) {
    val job = PrinterJob.getPrinterJob()
    job.jobName = "Ticket - ${receipt.movieName}"

    job.setPrintable { graphics, pageFormat, pageIndex ->
        if (pageIndex > 0) return@setPrintable Printable.NO_SUCH_PAGE

        val g = graphics as java.awt.Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.translate(pageFormat.imageableX.toInt(), pageFormat.imageableY.toInt())

        val leftMargin = 40
        var y = 40
        val lineGap = 22

        g.font = Font("Arial", Font.BOLD, 20)
        g.drawString("CANDY CINEMA", leftMargin, y); y += lineGap + 8

        g.font = Font("Arial", Font.PLAIN, 11)
        g.drawString("─".repeat(38), leftMargin, y); y += lineGap

        g.font = Font("Arial", Font.BOLD, 15)
        g.drawString(receipt.movieName, leftMargin, y); y += lineGap

        g.font = Font("Arial", Font.PLAIN, 12)
        g.drawString(receipt.startTime, leftMargin, y); y += lineGap
        g.drawString("Seats: ${receipt.seatNames.joinToString(", ")}", leftMargin, y); y += lineGap
        g.drawString("Total:  ${"$"}${"%.2f".format(receipt.totalPrice)}", leftMargin, y); y += lineGap + 8

        g.font = Font("Arial", Font.PLAIN, 11)
        g.drawString("─".repeat(38), leftMargin, y); y += lineGap + 4

        // QR code centered
        val qrSize = 160
        val pageWidth = pageFormat.imageableWidth.toInt()
        val qrX = (pageWidth - qrSize) / 2
        g.drawImage(qrImage, qrX, y, qrSize, qrSize, null); y += qrSize + 12

        g.font = Font("Courier New", Font.BOLD, 16)
        val codeWidth = g.fontMetrics.stringWidth(receipt.confirmationCode)
        g.drawString(receipt.confirmationCode, (pageWidth - codeWidth) / 2, y); y += lineGap

        g.font = Font("Arial", Font.ITALIC, 10)
        val hint = "Present this code at the theater entrance"
        val hintWidth = g.fontMetrics.stringWidth(hint)
        g.drawString(hint, (pageWidth - hintWidth) / 2, y)

        Printable.PAGE_EXISTS
    }

    if (job.printDialog()) {
        job.print()
    }
}