package com.receiptr.presentation.scan

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class PhotoManager(private val context: Context) {
    
    fun createImageUri(): Uri {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, 
                "receipt_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        
        return context.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: Uri.EMPTY
    }
    
    companion object {
        @OptIn(ExperimentalPermissionsApi::class)
        @Composable
        fun rememberPhotoLaunchers(
            onPhotoSelected: (Uri) -> Unit,
            onError: (String) -> Unit
        ): PhotoLaunchers {
            val context = LocalContext.current
            
            // Gallery permission state
            val galleryPermissionState = rememberPermissionState(
                permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    android.Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                }
            )
            
            // Camera permission state
            val cameraPermissionState = rememberPermissionState(
                permission = android.Manifest.permission.CAMERA
            )
            
            // Gallery launcher
            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    onPhotoSelected(it)
                } ?: onError("No image selected")
            }
            
            // Camera launcher
            val cameraUri = remember { mutableStateOf<Uri?>(null) }
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success: Boolean ->
                if (success) {
                    cameraUri.value?.let { uri ->
                        onPhotoSelected(uri)
                    }
                } else {
                    onError("Failed to capture image")
                }
            }
            
            return PhotoLaunchers(
                galleryPermissionState = galleryPermissionState,
                cameraPermissionState = cameraPermissionState,
                galleryLauncher = galleryLauncher,
                cameraLauncher = cameraLauncher,
                cameraUri = cameraUri,
                context = context
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
data class PhotoLaunchers(
    val galleryPermissionState: com.google.accompanist.permissions.PermissionState,
    val cameraPermissionState: com.google.accompanist.permissions.PermissionState,
    val galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    val cameraUri: MutableState<Uri?>,
    val context: Context
) {
    
    fun selectFromGallery() {
        if (galleryPermissionState.status.isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            galleryPermissionState.launchPermissionRequest()
        }
    }
    
    fun captureFromCamera() {
        if (cameraPermissionState.status.isGranted) {
            val photoManager = PhotoManager(context)
            val uri = photoManager.createImageUri()
            cameraUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}
