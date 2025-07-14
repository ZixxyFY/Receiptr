package com.receiptr.data.ml

import android.graphics.Point
import android.graphics.Rect

/**
 * Data models for text recognition results
 */

data class TextRecognitionResult(
    val fullText: String,
    val textBlocks: List<TextBlock>,
    val isSuccess: Boolean,
    val error: String? = null
)

data class TextBlock(
    val text: String,
    val boundingBox: Rect?,
    val cornerPoints: List<Point>,
    val lines: List<TextLine>
)

data class TextLine(
    val text: String,
    val boundingBox: Rect?,
    val cornerPoints: List<Point>,
    val elements: List<TextElement>
)

data class TextElement(
    val text: String,
    val boundingBox: Rect?,
    val cornerPoints: List<Point>
)

/**
 * Parsed receipt data from recognized text
 */
data class ReceiptData(
    val merchantName: String? = null,
    val merchantAddress: String? = null,
    val phoneNumber: String? = null,
    val date: String? = null,
    val time: String? = null,
    val items: List<ReceiptItem> = emptyList(),
    val subtotal: String? = null,
    val tax: String? = null,
    val total: String? = null,
    val paymentMethod: String? = null,
    val rawText: String = ""
)

data class ReceiptItem(
    val name: String,
    val quantity: String? = null,
    val price: String? = null,
    val total: String? = null
)
