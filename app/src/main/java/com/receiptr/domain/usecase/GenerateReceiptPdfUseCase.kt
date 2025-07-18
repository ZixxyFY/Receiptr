package com.receiptr.domain.usecase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.receiptr.domain.model.Receipt
import com.receiptr.domain.model.ReceiptItem
import com.receiptr.data.notification.NotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateReceiptPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {
    
    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40
        private const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN)
        
        private const val LOGO_TEXT_SIZE = 28f
        private const val HEADER_TEXT_SIZE = 16f
        private const val TITLE_TEXT_SIZE = 18f
        private const val NORMAL_TEXT_SIZE = 14f
        private const val SMALL_TEXT_SIZE = 12f
        private const val AMOUNT_TEXT_SIZE = 20f
        
        private const val LINE_SPACING = 20f
        private const val SECTION_SPACING = 30f
        private const val BORDER_WIDTH = 2f
        
        // Colors
        private const val BORDER_COLOR = Color.BLACK
        private const val BACKGROUND_COLOR = Color.WHITE
        private const val ACCENT_COLOR = Color.DKGRAY
    }
    
    suspend fun execute(receipt: Receipt): Uri = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        
        // Setup paint objects
        val logoPaint = Paint().apply {
            textSize = LOGO_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
            color = ACCENT_COLOR
        }
        
        val headerPaint = Paint().apply {
            textSize = HEADER_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
            color = ACCENT_COLOR
        }
        
        val titlePaint = Paint().apply {
            textSize = TITLE_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val normalPaint = Paint().apply {
            textSize = NORMAL_TEXT_SIZE
            isAntiAlias = true
        }
        
        val boldPaint = Paint().apply {
            textSize = NORMAL_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val amountPaint = Paint().apply {
            textSize = AMOUNT_TEXT_SIZE
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val smallPaint = Paint().apply {
            textSize = SMALL_TEXT_SIZE
            isAntiAlias = true
        }
        
        val borderPaint = Paint().apply {
            color = BORDER_COLOR
            strokeWidth = BORDER_WIDTH
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        
        val fillPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        // Draw outer border
        drawOuterBorder(canvas, borderPaint)
        
        // Draw header section
        var currentY = drawCustomHeader(canvas, logoPaint, headerPaint, currentY = MARGIN.toFloat() + 20f)
        
        // Draw receipt details section
        currentY = drawCustomReceiptDetails(canvas, normalPaint, boldPaint, amountPaint, fillPaint, borderPaint, receipt, currentY + 30f)
        
        // Draw notes section if available
        if (receipt.notes.isNotEmpty() || receipt.ocrText.isNotEmpty()) {
            currentY = drawNotesSection(canvas, normalPaint, boldPaint, borderPaint, receipt, currentY + 30f)
        }
        
        // Draw footer
        drawCustomFooter(canvas, smallPaint, borderPaint)
        
        pdfDocument.finishPage(page)
        
        // Save PDF to cache directory
        val pdfFile = File(context.cacheDir, "receipt_${receipt.id}_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()
        
        // Send PDF export notification
        notificationManager.sendPdfExportNotification(receipt.id, pdfFile.absolutePath)
        
        // Generate content URI using FileProvider
        return@withContext FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }
    
    
    
    private fun drawKeyValuePair(
        canvas: Canvas,
        keyPaint: Paint,
        valuePaint: Paint,
        leftX: Float,
        y: Float,
        key: String,
        value: String
    ): Float {
        canvas.drawText(key, leftX, y + keyPaint.textSize, keyPaint)
        
        // Calculate value position (leave space after key)
        val keyWidth = keyPaint.measureText(key)
        val valueX = leftX + keyWidth + 40 // Add some spacing
        canvas.drawText(value, valueX, y + valuePaint.textSize, valuePaint)
        
        return LINE_SPACING
    }
    
    private fun drawOuterBorder(canvas: Canvas, borderPaint: Paint) {
        val rect = RectF(MARGIN.toFloat(), MARGIN.toFloat(), PAGE_WIDTH - MARGIN.toFloat(), PAGE_HEIGHT - MARGIN.toFloat())
        canvas.drawRect(rect, borderPaint)
    }

    private fun drawCustomHeader(
        canvas: Canvas,
        logoPaint: Paint,
        headerPaint: Paint,
        currentY: Float
    ): Float {
        var y = currentY
        // Drawing logo
        canvas.drawText("[Receiptr Logo]", MARGIN.toFloat() + 10, y, logoPaint)
        // Drawing "Receipt Details"
        y += logoPaint.textSize + LINE_SPACING
        canvas.drawText("Receipt Details", (PAGE_WIDTH / 2).toFloat(), y, headerPaint)
        // Drawing export date
        y += headerPaint.textSize + LINE_SPACING
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        canvas.drawText("Exported on: $currentDate", (PAGE_WIDTH / 2).toFloat(), y, headerPaint)
        return y + SECTION_SPACING
    }

    private fun drawCustomReceiptDetails(
        canvas: Canvas,
        normalPaint: Paint,
        boldPaint: Paint,
        amountPaint: Paint,
        fillPaint: Paint,
        borderPaint: Paint,
        receipt: Receipt,
        currentY: Float
    ): Float {
        var y = currentY
        val leftX = MARGIN.toFloat() + 20
        // Drawing Merchant
        y += drawKeyValuePair(canvas, boldPaint, normalPaint, leftX, y, "Merchant:", receipt.merchantName.ifEmpty { "Unknown" })
        // Drawing Date
        y += drawKeyValuePair(canvas, boldPaint, normalPaint, leftX, y, "Date:", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(receipt.date)))
        // Drawing Category
        y += drawKeyValuePair(canvas, boldPaint, normalPaint, leftX, y, "Category:", receipt.category)
        // Drawing total amount with a border
        val rectY = y + LINE_SPACING
        val rect = RectF(MARGIN.toFloat() + 20, rectY, PAGE_WIDTH - MARGIN.toFloat() - 20, rectY + LINE_SPACING * 2)
        canvas.drawRoundRect(rect, 10f, 10f, fillPaint)
        canvas.drawRoundRect(rect, 10f, 10f, borderPaint)
        canvas.drawText("Total Amount: ${receipt.currency} ${String.format("%.2f", receipt.totalAmount)}", leftX + 10, rectY + amountPaint.textSize + 10, amountPaint)
        return rect.bottom
    }

    private fun drawNotesSection(
        canvas: Canvas,
        normalPaint: Paint,
        boldPaint: Paint,
        borderPaint: Paint,
        receipt: Receipt,
        currentY: Float
    ): Float {
        var y = currentY
        val leftX = MARGIN.toFloat() + 20
        
        // Determine notes content
        val notesContent = when {
            receipt.notes.isNotEmpty() -> receipt.notes
            receipt.ocrText.isNotEmpty() -> "Auto-processed receipt"
            else -> "No additional notes"
        }
        
        // Drawing Notes section
        val rectY = y + LINE_SPACING
        val rect = RectF(MARGIN.toFloat() + 20, rectY, PAGE_WIDTH - MARGIN.toFloat() - 20, rectY + LINE_SPACING * 4)
        canvas.drawRoundRect(rect, 10f, 10f, borderPaint)
        
        // Draw notes title
        canvas.drawText("Notes:", leftX + 10, rectY + boldPaint.textSize + 10, boldPaint)
        
        // Draw notes content with basic text wrapping
        val maxWidth = rect.width() - 40 // Leave some padding
        val wrappedText = wrapText(notesContent, normalPaint, maxWidth)
        var textY = rectY + boldPaint.textSize + LINE_SPACING + 10
        wrappedText.forEach { line ->
            canvas.drawText(line, leftX + 10, textY, normalPaint)
            textY += normalPaint.textSize + 5
        }
        
        return rect.bottom
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    // Single word is too long, truncate it
                    lines.add(word.take(50) + "...")
                    currentLine = ""
                }
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }

    private fun drawCustomFooter(
        canvas: Canvas,
        smallPaint: Paint,
        borderPaint: Paint
    ) {
        val y = PAGE_HEIGHT - MARGIN.toFloat() - 40
        val leftX = MARGIN.toFloat() + 20
        // Drawing footer content
        canvas.drawText("Generated by Receiptr", leftX, y, smallPaint)
        canvas.drawText("© 2025 Receiptr. All rights reserved. www.receiptr-app.com", (PAGE_WIDTH / 2).toFloat(), y, smallPaint)
    }
}
