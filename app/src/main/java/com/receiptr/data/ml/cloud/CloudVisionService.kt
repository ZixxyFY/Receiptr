package com.receiptr.data.ml.cloud

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.receiptr.data.ml.TextRecognitionResult
import com.receiptr.data.ml.TextBlock
import com.receiptr.data.ml.TextLine
import com.receiptr.data.ml.TextElement
import com.receiptr.data.ml.schema.ReceiptSchema
import com.receiptr.data.ml.schema.LineItem
import com.receiptr.data.ml.schema.ReceiptCategory
import com.receiptr.data.ml.schema.PaymentMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Cloud Vision API service for backup OCR and Document AI integration
 */
@Singleton
class CloudVisionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "CloudVisionService"
        private const val CLOUD_VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate"
        private const val DOCUMENT_AI_API_URL = "https://documentai.googleapis.com/v1/projects"
        private const val RECEIPT_PROCESSOR_TYPE = "RECEIPT_PROCESSOR"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    /**
     * Perform OCR using Google Cloud Vision API
     */
    suspend fun performOCR(
        bitmap: Bitmap,
        apiKey: String,
        features: List<CloudVisionFeature> = listOf(CloudVisionFeature.TEXT_DETECTION)
    ): CloudVisionResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Performing OCR with Cloud Vision API")
        
        try {
            val base64Image = bitmapToBase64(bitmap)
            val requestBody = createOCRRequestBody(base64Image, features)
            
            val request = Request.Builder()
                .url("$CLOUD_VISION_API_URL?key=$apiKey")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = executeWithRetry(request)
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            
            if (!response.isSuccessful) {
                throw Exception("Cloud Vision API error: ${response.code} - $responseBody")
            }
            
            parseOCRResponse(responseBody)
            
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed", e)
            CloudVisionResult(
                success = false,
                error = e.message,
                confidence = 0.0f,
                textAnnotations = emptyList(),
                fullTextAnnotation = null,
                processingTimeMs = 0
            )
        }
    }
    
    /**
     * Process receipt using Google Document AI Receipt Processor
     */
    suspend fun processReceiptWithDocumentAI(
        bitmap: Bitmap,
        apiKey: String,
        projectId: String,
        locationId: String = "us",
        processorId: String
    ): DocumentAIResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Processing receipt with Document AI")
        
        try {
            val base64Image = bitmapToBase64(bitmap)
            val requestBody = createDocumentAIRequestBody(base64Image)
            
            val url = "$DOCUMENT_AI_API_URL/$projectId/locations/$locationId/processors/$processorId:process"
            val request = Request.Builder()
                .url("$url?key=$apiKey")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = executeWithRetry(request)
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            
            if (!response.isSuccessful) {
                throw Exception("Document AI error: ${response.code} - $responseBody")
            }
            
            parseDocumentAIResponse(responseBody)
            
        } catch (e: Exception) {
            Log.e(TAG, "Document AI processing failed", e)
            DocumentAIResult(
                success = false,
                error = e.message,
                confidence = 0.0f,
                extractedReceipt = null,
                rawResponse = null,
                processingTimeMs = 0
            )
        }
    }
    
    /**
     * Hybrid processing: Try Document AI first, fallback to Cloud Vision OCR
     */
    suspend fun hybridReceiptProcessing(
        bitmap: Bitmap,
        apiKey: String,
        projectId: String,
        processorId: String,
        fallbackToOCR: Boolean = true
    ): HybridProcessingResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting hybrid receipt processing")
        
        val startTime = System.currentTimeMillis()
        
        // Try Document AI first
        val documentAIResult = processReceiptWithDocumentAI(
            bitmap, apiKey, projectId, "us", processorId
        )
        
        if (documentAIResult.success && documentAIResult.confidence > 0.7f) {
            Log.d(TAG, "Document AI processing successful")
            return@withContext HybridProcessingResult(
                success = true,
                primaryMethod = ProcessingMethod.DOCUMENT_AI,
                fallbackUsed = false,
                documentAIResult = documentAIResult,
                cloudVisionResult = null,
                combinedConfidence = documentAIResult.confidence,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
        
        // Fallback to Cloud Vision OCR if enabled
        if (fallbackToOCR) {
            Log.d(TAG, "Falling back to Cloud Vision OCR")
            val ocrResult = performOCR(bitmap, apiKey)
            
            if (ocrResult.success) {
                // Convert OCR result to receipt schema
                val extractedReceipt = convertOCRToReceiptSchema(ocrResult)
                
                return@withContext HybridProcessingResult(
                    success = true,
                    primaryMethod = ProcessingMethod.CLOUD_VISION,
                    fallbackUsed = true,
                    documentAIResult = documentAIResult,
                    cloudVisionResult = ocrResult,
                    extractedReceipt = extractedReceipt,
                    combinedConfidence = ocrResult.confidence,
                    processingTimeMs = System.currentTimeMillis() - startTime
                )
            }
        }
        
        // Both methods failed
        HybridProcessingResult(
            success = false,
            primaryMethod = ProcessingMethod.DOCUMENT_AI,
            fallbackUsed = fallbackToOCR,
            documentAIResult = documentAIResult,
            cloudVisionResult = null,
            combinedConfidence = 0.0f,
            processingTimeMs = System.currentTimeMillis() - startTime,
            error = "Both Document AI and Cloud Vision processing failed"
        )
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
    }
    
    private fun createOCRRequestBody(base64Image: String, features: List<CloudVisionFeature>): RequestBody {
        val requestJson = JSONObject().apply {
            put("requests", JSONArray().apply {
                put(JSONObject().apply {
                    put("image", JSONObject().apply {
                        put("content", base64Image)
                    })
                    put("features", JSONArray().apply {
                        features.forEach { feature ->
                            put(JSONObject().apply {
                                put("type", feature.apiName)
                                put("maxResults", feature.maxResults)
                            })
                        }
                    })
                })
            })
        }
        
        return requestJson.toString().toRequestBody("application/json".toMediaType())
    }
    
    private fun createDocumentAIRequestBody(base64Image: String): RequestBody {
        val requestJson = JSONObject().apply {
            put("rawDocument", JSONObject().apply {
                put("content", base64Image)
                put("mimeType", "image/jpeg")
            })
        }
        
        return requestJson.toString().toRequestBody("application/json".toMediaType())
    }
    
    private suspend fun executeWithRetry(request: Request): Response {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful || response.code < 500) {
                    return response
                }
                lastException = Exception("HTTP ${response.code}: ${response.message}")
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Request attempt ${attempt + 1} failed", e)
            }
            
            if (attempt < MAX_RETRIES - 1) {
                kotlinx.coroutines.delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }
        
        throw lastException ?: Exception("Max retries exceeded")
    }
    
    private fun parseOCRResponse(responseBody: String): CloudVisionResult {
        val startTime = System.currentTimeMillis()
        
        try {
            val jsonResponse = JSONObject(responseBody)
            val responses = jsonResponse.getJSONArray("responses")
            
            if (responses.length() == 0) {
                throw Exception("No responses in Cloud Vision API result")
            }
            
            val firstResponse = responses.getJSONObject(0)
            
            // Check for errors
            if (firstResponse.has("error")) {
                val error = firstResponse.getJSONObject("error")
                throw Exception("Cloud Vision API error: ${error.optString("message", "Unknown error")}")
            }
            
            val textAnnotations = mutableListOf<TextAnnotation>()
            var fullTextAnnotation: FullTextAnnotation? = null
            
            // Parse text annotations
            if (firstResponse.has("textAnnotations")) {
                val annotations = firstResponse.getJSONArray("textAnnotations")
                for (i in 0 until annotations.length()) {
                    val annotation = annotations.getJSONObject(i)
                    textAnnotations.add(parseTextAnnotation(annotation))
                }
            }
            
            // Parse full text annotation
            if (firstResponse.has("fullTextAnnotation")) {
                val fullText = firstResponse.getJSONObject("fullTextAnnotation")
                fullTextAnnotation = parseFullTextAnnotation(fullText)
            }
            
            val confidence = calculateOverallConfidence(textAnnotations)
            
            return CloudVisionResult(
                success = true,
                confidence = confidence,
                textAnnotations = textAnnotations,
                fullTextAnnotation = fullTextAnnotation,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing OCR response", e)
            throw e
        }
    }
    
    private fun parseDocumentAIResponse(responseBody: String): DocumentAIResult {
        val startTime = System.currentTimeMillis()
        
        try {
            val jsonResponse = JSONObject(responseBody)
            val document = jsonResponse.getJSONObject("document")
            
            // Extract receipt data from Document AI response
            val extractedReceipt = parseDocumentAIReceiptData(document)
            val confidence = calculateDocumentAIConfidence(document)
            
            return DocumentAIResult(
                success = true,
                confidence = confidence,
                extractedReceipt = extractedReceipt,
                rawResponse = responseBody,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Document AI response", e)
            throw e
        }
    }
    
    private fun parseTextAnnotation(annotation: JSONObject): TextAnnotation {
        val description = annotation.optString("description", "")
        val boundingPoly = annotation.optJSONObject("boundingPoly")
        val confidence = annotation.optDouble("confidence", 0.0).toFloat()
        
        return TextAnnotation(
            description = description,
            boundingBox = parseBoundingBox(boundingPoly),
            confidence = confidence
        )
    }
    
    private fun parseFullTextAnnotation(fullText: JSONObject): FullTextAnnotation {
        val text = fullText.optString("text", "")
        val pages = mutableListOf<TextPage>()
        
        if (fullText.has("pages")) {
            val pagesArray = fullText.getJSONArray("pages")
            for (i in 0 until pagesArray.length()) {
                pages.add(parseTextPage(pagesArray.getJSONObject(i)))
            }
        }
        
        return FullTextAnnotation(
            text = text,
            pages = pages
        )
    }
    
    private fun parseTextPage(page: JSONObject): TextPage {
        val blocks = mutableListOf<TextBlock>()
        val confidence = page.optDouble("confidence", 0.0).toFloat()
        
        if (page.has("blocks")) {
            val blocksArray = page.getJSONArray("blocks")
            for (i in 0 until blocksArray.length()) {
                blocks.add(parseTextBlock(blocksArray.getJSONObject(i)))
            }
        }
        
        return TextPage(
            blocks = blocks,
            confidence = confidence
        )
    }
    
    private fun parseTextBlock(block: JSONObject): TextBlock {
        val paragraphs = mutableListOf<TextParagraph>()
        val boundingBox = parseBoundingBox(block.optJSONObject("boundingBox"))
        val confidence = block.optDouble("confidence", 0.0).toFloat()
        
        if (block.has("paragraphs")) {
            val paragraphsArray = block.getJSONArray("paragraphs")
            for (i in 0 until paragraphsArray.length()) {
                paragraphs.add(parseTextParagraph(paragraphsArray.getJSONObject(i)))
            }
        }
        
        return com.receiptr.data.ml.TextBlock(
            text = paragraphs.joinToString(" ") { it.words.joinToString(" ") { word -> word.symbols.joinToString("") { it.text } } },
            boundingBox = boundingBox,
            cornerPoints = emptyList(),
            lines = paragraphs.flatMap { paragraph ->
                paragraph.words.map { word ->
                    com.receiptr.data.ml.TextLine(
                        text = word.symbols.joinToString("") { it.text },
                        boundingBox = word.boundingBox,
                        cornerPoints = emptyList(),
                        elements = word.symbols.map { symbol ->
                            com.receiptr.data.ml.TextElement(
                                text = symbol.text,
                                boundingBox = symbol.boundingBox,
                                cornerPoints = emptyList()
                            )
                        }
                    )
                }
            }
        )
    }
    
    private fun parseTextParagraph(paragraph: JSONObject): TextParagraph {
        val words = mutableListOf<TextWord>()
        val boundingBox = parseBoundingBox(paragraph.optJSONObject("boundingBox"))
        val confidence = paragraph.optDouble("confidence", 0.0).toFloat()
        
        if (paragraph.has("words")) {
            val wordsArray = paragraph.getJSONArray("words")
            for (i in 0 until wordsArray.length()) {
                words.add(parseTextWord(wordsArray.getJSONObject(i)))
            }
        }
        
        return TextParagraph(
            words = words,
            boundingBox = boundingBox,
            confidence = confidence
        )
    }
    
    private fun parseTextWord(word: JSONObject): TextWord {
        val symbols = mutableListOf<TextSymbol>()
        val boundingBox = parseBoundingBox(word.optJSONObject("boundingBox"))
        val confidence = word.optDouble("confidence", 0.0).toFloat()
        
        if (word.has("symbols")) {
            val symbolsArray = word.getJSONArray("symbols")
            for (i in 0 until symbolsArray.length()) {
                symbols.add(parseTextSymbol(symbolsArray.getJSONObject(i)))
            }
        }
        
        return TextWord(
            symbols = symbols,
            boundingBox = boundingBox,
            confidence = confidence
        )
    }
    
    private fun parseTextSymbol(symbol: JSONObject): TextSymbol {
        val text = symbol.optString("text", "")
        val boundingBox = parseBoundingBox(symbol.optJSONObject("boundingBox"))
        val confidence = symbol.optDouble("confidence", 0.0).toFloat()
        
        return TextSymbol(
            text = text,
            boundingBox = boundingBox,
            confidence = confidence
        )
    }
    
    private fun parseBoundingBox(boundingPoly: JSONObject?): android.graphics.Rect? {
        if (boundingPoly == null) return null
        
        val vertices = boundingPoly.optJSONArray("vertices") ?: return null
        if (vertices.length() < 4) return null
        
        val points = mutableListOf<android.graphics.Point>()
        for (i in 0 until vertices.length()) {
            val vertex = vertices.getJSONObject(i)
            val x = vertex.optInt("x", 0)
            val y = vertex.optInt("y", 0)
            points.add(android.graphics.Point(x, y))
        }
        
        val left = points.minOfOrNull { it.x } ?: 0
        val top = points.minOfOrNull { it.y } ?: 0
        val right = points.maxOfOrNull { it.x } ?: 0
        val bottom = points.maxOfOrNull { it.y } ?: 0
        
        return android.graphics.Rect(left, top, right, bottom)
    }
    
    private fun calculateOverallConfidence(textAnnotations: List<TextAnnotation>): Float {
        if (textAnnotations.isEmpty()) return 0.0f
        return textAnnotations.map { it.confidence }.average().toFloat()
    }
    
    private fun calculateDocumentAIConfidence(document: JSONObject): Float {
        // Document AI provides confidence scores in entities
        val entities = document.optJSONArray("entities") ?: return 0.5f
        
        var totalConfidence = 0.0f
        var count = 0
        
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val confidence = entity.optDouble("confidence", 0.0).toFloat()
            totalConfidence += confidence
            count++
        }
        
        return if (count > 0) totalConfidence / count else 0.5f
    }
    
    private fun parseDocumentAIReceiptData(document: JSONObject): ReceiptSchema {
        val entities = document.optJSONArray("entities") ?: JSONArray()
        val text = document.optString("text", "")
        
        var merchantName: String? = null
        var totalAmount: Float? = null
        var subtotalAmount: Float? = null
        var taxAmount: Float? = null
        var transactionDate: Date? = null
        var transactionTime: String? = null
        val lineItems = mutableListOf<LineItem>()
        
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val type = entity.optString("type", "")
            val mentionText = entity.optString("mentionText", "")
            val confidence = entity.optDouble("confidence", 0.0).toFloat()
            
            when (type) {
                "supplier_name" -> merchantName = mentionText
                "total_amount" -> totalAmount = parseAmount(mentionText)
                "subtotal_amount" -> subtotalAmount = parseAmount(mentionText)
                "tax_amount" -> taxAmount = parseAmount(mentionText)
                "receipt_date" -> transactionDate = parseDate(mentionText)
                "receipt_time" -> transactionTime = mentionText
                "line_item" -> {
                    // Parse line item from nested properties
                    val properties = entity.optJSONArray("properties")
                    if (properties != null) {
                        val lineItem = parseLineItem(properties)
                        if (lineItem != null) {
                            lineItems.add(lineItem)
                        }
                    }
                }
            }
        }
        
        return ReceiptSchema(
            merchantName = merchantName,
            merchantAddress = null,
            phoneNumber = null,
            transactionDate = transactionDate,
            transactionTime = transactionTime,
            totalAmount = totalAmount,
            subtotalAmount = subtotalAmount,
            taxAmount = taxAmount,
            tipAmount = null,
            discountAmount = null,
            lineItems = lineItems,
            category = ReceiptCategory.OTHER,
            paymentMethod = null,
            rawText = text,
            confidence = 0.8f,
            imageUri = null,
            annotationNotes = "Extracted using Google Document AI"
        )
    }
    
    private fun parseLineItem(properties: JSONArray): LineItem? {
        var itemName: String? = null
        var quantity: Float? = null
        var unitPrice: Float? = null
        var totalPrice: Float? = null
        
        for (i in 0 until properties.length()) {
            val property = properties.getJSONObject(i)
            val type = property.optString("type", "")
            val mentionText = property.optString("mentionText", "")
            
            when (type) {
                "line_item_description" -> itemName = mentionText
                "line_item_quantity" -> quantity = mentionText.toFloatOrNull()
                "line_item_unit_price" -> unitPrice = parseAmount(mentionText)
                "line_item_total_price" -> totalPrice = parseAmount(mentionText)
            }
        }
        
        return if (itemName != null && totalPrice != null) {
            LineItem(
                name = itemName,
                description = null,
                quantity = quantity ?: 1.0f,
                unitPrice = unitPrice,
                totalPrice = totalPrice,
                category = null,
                sku = null,
                barcode = null,
                discount = null,
                tax = null
            )
        } else null
    }
    
    private fun parseAmount(amountString: String): Float? {
        return try {
            amountString.replace(Regex("[^0-9.]"), "").toFloat()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDate(dateString: String): Date? {
        val dateFormats = arrayOf(
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "yyyy-MM-dd",
            "MMM dd, yyyy",
            "dd MMM yyyy"
        )
        
        for (format in dateFormats) {
            try {
                val sdf = java.text.SimpleDateFormat(format, Locale.US)
                return sdf.parse(dateString)
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        return null
    }
    
    private fun convertOCRToReceiptSchema(ocrResult: CloudVisionResult): ReceiptSchema {
        val fullText = ocrResult.fullTextAnnotation?.text ?: ""
        
        // Use existing ML Kit parser logic with Cloud Vision data
        // This is a simplified conversion - you would integrate with ReceiptParserService
        
        return ReceiptSchema(
            merchantName = extractMerchantNameFromOCR(ocrResult),
            merchantAddress = null,
            phoneNumber = null,
            transactionDate = null,
            transactionTime = null,
            totalAmount = extractTotalAmountFromOCR(ocrResult),
            subtotalAmount = null,
            taxAmount = null,
            tipAmount = null,
            discountAmount = null,
            lineItems = emptyList(),
            category = ReceiptCategory.OTHER,
            paymentMethod = null,
            rawText = fullText,
            confidence = ocrResult.confidence,
            imageUri = null,
            annotationNotes = "Extracted using Google Cloud Vision API"
        )
    }
    
    private fun extractMerchantNameFromOCR(ocrResult: CloudVisionResult): String? {
        // Simple extraction - first non-empty text annotation
        return ocrResult.textAnnotations.firstOrNull()?.description?.takeIf { it.isNotBlank() }
    }
    
    private fun extractTotalAmountFromOCR(ocrResult: CloudVisionResult): Float? {
        val text = ocrResult.fullTextAnnotation?.text ?: return null
        val totalPattern = Regex("(?i)total\\s*:?\\s*\\$?([0-9]{1,3}(?:,?[0-9]{3})*(?:\\.[0-9]{2})?)")
        val match = totalPattern.find(text)
        return match?.groupValues?.get(1)?.replace(",", "")?.toFloatOrNull()
    }
}

// Data classes for Cloud Vision API
data class CloudVisionResult(
    val success: Boolean,
    val confidence: Float,
    val textAnnotations: List<TextAnnotation>,
    val fullTextAnnotation: FullTextAnnotation?,
    val processingTimeMs: Long,
    val error: String? = null
)

data class DocumentAIResult(
    val success: Boolean,
    val confidence: Float,
    val extractedReceipt: ReceiptSchema?,
    val rawResponse: String?,
    val processingTimeMs: Long,
    val error: String? = null
)

data class HybridProcessingResult(
    val success: Boolean,
    val primaryMethod: ProcessingMethod,
    val fallbackUsed: Boolean,
    val documentAIResult: DocumentAIResult,
    val cloudVisionResult: CloudVisionResult?,
    val extractedReceipt: ReceiptSchema? = null,
    val combinedConfidence: Float,
    val processingTimeMs: Long,
    val error: String? = null
)

enum class ProcessingMethod {
    DOCUMENT_AI,
    CLOUD_VISION,
    ML_KIT
}

enum class CloudVisionFeature(val apiName: String, val maxResults: Int = 10) {
    TEXT_DETECTION("TEXT_DETECTION", 1),
    DOCUMENT_TEXT_DETECTION("DOCUMENT_TEXT_DETECTION", 1),
    OBJECT_LOCALIZATION("OBJECT_LOCALIZATION", 10),
    LOGO_DETECTION("LOGO_DETECTION", 10)
}

data class TextAnnotation(
    val description: String,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)

data class FullTextAnnotation(
    val text: String,
    val pages: List<TextPage>
)

data class TextPage(
    val blocks: List<TextBlock>,
    val confidence: Float
)

data class TextBlock(
    val paragraphs: List<TextParagraph>,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)

data class TextParagraph(
    val words: List<TextWord>,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)

data class TextWord(
    val symbols: List<TextSymbol>,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)

data class TextSymbol(
    val text: String,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)
