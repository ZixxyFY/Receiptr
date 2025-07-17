package com.receiptr.presentation.scan

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import com.receiptr.data.ml.preprocessing.ImagePreprocessingService
import com.receiptr.data.ml.preprocessing.PreprocessingOptions
import com.receiptr.data.ml.preprocessing.PreprocessedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Example integration of ImagePreprocessingService with CameraX
 */
@Singleton
class ImagePreprocessingExample @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preprocessingService: ImagePreprocessingService
) {
    
    /**
     * Create a CameraX ImageAnalysis use case with preprocessing
     */
    fun createPreprocessingImageAnalysis(
        onPreprocessedImage: (PreprocessedImage) -> Unit
    ): ImageAnalysis {
        
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            
        // Create the analyzer with preprocessing
        val analyzer = preprocessingService.ReceiptImageAnalyzer { preprocessedImage ->
            onPreprocessedImage(preprocessedImage)
        }
        
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer
        )
        
        return imageAnalysis
    }
    
    /**
     * Full preprocessing pipeline for captured images
     */
    suspend fun processReceiptImage(
        bitmap: Bitmap,
        enableAllFeatures: Boolean = true
    ): PreprocessedImage {
        
        val options = if (enableAllFeatures) {
            PreprocessingOptions(
                enablePerspectiveCorrection = true,
                enableNoiseReduction = true,
                enableContrastEnhancement = true,
                enableSharpening = true,
                enableBinarization = false, // Usually better without for ML Kit
                enableResolutionEnhancement = true,
                enableGrayscaleConversion = true,
                contrastFactor = 1.3f,
                scaleFactor = 1.2f // Slight upscaling for better text recognition
            )
        } else {
            PreprocessingOptions(
                enablePerspectiveCorrection = true,
                enableContrastEnhancement = true,
                enableGrayscaleConversion = true
            )
        }
        
        return preprocessingService.preprocessReceiptImage(bitmap, options)
    }
    
    /**
     * Quick preprocessing for real-time preview
     */
    suspend fun processForPreview(bitmap: Bitmap): PreprocessedImage {
        val previewOptions = PreprocessingOptions(
            enablePerspectiveCorrection = false,
            enableNoiseReduction = false,
            enableContrastEnhancement = true,
            enableSharpening = false,
            enableBinarization = false,
            enableResolutionEnhancement = false,
            enableGrayscaleConversion = true,
            contrastFactor = 1.2f
        )
        
        return preprocessingService.preprocessReceiptImage(bitmap, previewOptions)
    }
}

/**
 * Usage example in a ViewModel or Activity
 */
class PreprocessingUsageExample @Inject constructor(
    private val preprocessingExample: ImagePreprocessingExample
) {
    
    fun setupCamera() {
        // Create image analysis with preprocessing
        val imageAnalysis = preprocessingExample.createPreprocessingImageAnalysis { preprocessedImage ->
            // Handle the preprocessed image
            if (preprocessedImage.isSuccess) {
                // Show preview or quality indicators
                val qualityScore = preprocessedImage.qualityScore
                val processingTime = preprocessedImage.processingTime
                
                // You can show this to the user as feedback
                println("Image quality: ${(qualityScore * 100).toInt()}%")
                println("Processing time: ${processingTime}ms")
                
                // Display processing steps
                preprocessedImage.processingSteps.forEach { step ->
                    println("${step.name}: ${if (step.applied) "Applied" else "Skipped"} (${(step.confidence * 100).toInt()}% confidence)")
                }
            }
        }
        
        // Add to camera binding
        // cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)
    }
    
    fun processImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            // Process the image with full preprocessing
            val result = preprocessingExample.processReceiptImage(bitmap)
            
            if (result.isSuccess) {
                // Use the processed bitmap for text recognition
                val processedBitmap = result.processedBitmap
                
                // Pass to ML Kit Text Recognition
                // textRecognitionService.extractTextFromBitmap(processedBitmap)
            } else {
                // Handle preprocessing failure
                println("Preprocessing failed: ${result.error}")
            }
        }
    }
}
