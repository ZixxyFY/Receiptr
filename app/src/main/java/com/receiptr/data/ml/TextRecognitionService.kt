package com.receiptr.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.receiptr.data.ml.preprocessing.ImagePreprocessingService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service class for text recognition using Google ML Kit v2
 */
@Singleton
class TextRecognitionService @Inject constructor(
    private val context: Context
) {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Extracts text from an image bitmap
     */
suspend fun extractTextFromBitmap(bitmap: Bitmap): TextRecognitionResult = withContext(Dispatchers.IO) {
        val preprocessService = ImagePreprocessingService()
        val preprocessedImage = preprocessService.preprocessReceiptImage(bitmap)
        if (!preprocessedImage.isSuccess) {
            throw Exception("Preprocessing failed: ${preprocessedImage.error}")
        }
        
        val processedBitmap = preprocessedImage.processedBitmap
        val image = InputImage.fromBitmap(processedBitmap, 0)
        
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    val textBlocks = visionText.textBlocks.map { block ->
                        TextBlock(
                            text = block.text,
                            boundingBox = block.boundingBox,
                            cornerPoints = block.cornerPoints?.toList() ?: emptyList(),
                            lines = block.lines.map { line ->
                                TextLine(
                                    text = line.text,
                                    boundingBox = line.boundingBox,
                                    cornerPoints = line.cornerPoints?.toList() ?: emptyList(),
                                    elements = line.elements.map { element ->
                                        TextElement(
                                            text = element.text,
                                            boundingBox = element.boundingBox,
                                            cornerPoints = element.cornerPoints?.toList() ?: emptyList()
                                        )
                                    }
                                )
                            }
                        )
                    }
                    
                    val result = TextRecognitionResult(
                        fullText = extractedText,
                        textBlocks = textBlocks,
                        isSuccess = true
                    )
                    
                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
    
    /**
     * Extracts text from an ImageBitmap (Compose)
     */
    suspend fun extractTextFromImageBitmap(imageBitmap: ImageBitmap): TextRecognitionResult {
        return extractTextFromBitmap(imageBitmap.asAndroidBitmap())
    }
    
    /**
     * Extracts text from an image URI
     */
    suspend fun extractTextFromUri(uri: Uri): TextRecognitionResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        val textBlocks = visionText.textBlocks.map { block ->
                            TextBlock(
                                text = block.text,
                                boundingBox = block.boundingBox,
                                cornerPoints = block.cornerPoints?.toList() ?: emptyList(),
                                lines = block.lines.map { line ->
                                    TextLine(
                                        text = line.text,
                                        boundingBox = line.boundingBox,
                                        cornerPoints = line.cornerPoints?.toList() ?: emptyList(),
                                        elements = line.elements.map { element ->
                                            TextElement(
                                                text = element.text,
                                                boundingBox = element.boundingBox,
                                                cornerPoints = element.cornerPoints?.toList() ?: emptyList()
                                            )
                                        }
                                    )
                                }
                            )
                        }
                        
                        val result = TextRecognitionResult(
                            fullText = extractedText,
                            textBlocks = textBlocks,
                            isSuccess = true
                        )
                        
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Cleans up the text recognizer resources
     */
    fun close() {
        recognizer.close()
    }
}
