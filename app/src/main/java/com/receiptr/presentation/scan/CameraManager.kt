package com.receiptr.presentation.scan

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraManager(private val context: Context) {
    
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    
    suspend fun initializeCamera(): ProcessCameraProvider {
        return suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    continuation.resume(provider)
                } catch (exception: Exception) {
                    continuation.resumeWithException(exception)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }
    
    fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        this.cameraProvider = cameraProvider
        this.preview = preview
        
        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            
            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exception: Exception) {
            // Handle camera binding error
        }
    }
    
    fun takePhoto(
        onImageCaptured: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: return
        
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { onImageCaptured(it) }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
    
    fun switchCamera(): CameraSelector {
        return if (camera?.cameraInfo?.lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
    
    fun hasFlash(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() ?: false
    }
    
    fun toggleFlash() {
        camera?.cameraControl?.enableTorch(
            camera?.cameraInfo?.torchState?.value == TorchState.OFF
        )
    }
    
    fun getZoomRatio(): Float {
        return camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
    }
    
    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }
    
    fun release() {
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageCapture = null
        cameraProvider = null
    }
}
