# Google ML Kit Text Recognition v2 Integration

## Overview
This document describes the implementation of Google ML Kit Text Recognition v2 in the Receiptr app to extract text and structured data from receipt images.

## Implementation Details

### 1. Dependencies Added
```kotlin
// Google ML Kit Text Recognition v2
implementation("com.google.mlkit:text-recognition:16.0.0")
```

### 2. Core Components

#### TextRecognitionService
- **Location**: `com.receiptr.data.ml.TextRecognitionService`
- **Purpose**: Handles ML Kit text recognition from images
- **Key Methods**:
  - `extractTextFromBitmap(bitmap: Bitmap)`: Extract text from Android Bitmap
  - `extractTextFromImageBitmap(imageBitmap: ImageBitmap)`: Extract text from Compose ImageBitmap
  - `extractTextFromUri(uri: Uri)`: Extract text from image URI

#### ReceiptParserService
- **Location**: `com.receiptr.data.ml.ReceiptParserService`
- **Purpose**: Parses structured receipt data from extracted text
- **Features**:
  - Merchant name detection
  - Date and time extraction
  - Total amount and tax extraction
  - Individual item parsing
  - Payment method detection

#### ProcessReceiptImageUseCase
- **Location**: `com.receiptr.domain.usecase.ProcessReceiptImageUseCase`
- **Purpose**: Orchestrates text recognition and receipt parsing
- **Key Methods**:
  - `processReceiptFromBitmap(bitmap: Bitmap)`: Complete receipt processing from bitmap
  - `processReceiptFromUri(uri: Uri)`: Complete receipt processing from URI
  - `extractRawTextFromBitmap(bitmap: Bitmap)`: Extract raw text only

### 3. Data Models

#### TextRecognitionResult
Contains the raw text recognition results from ML Kit:
```kotlin
data class TextRecognitionResult(
    val fullText: String,
    val textBlocks: List<TextBlock>,
    val isSuccess: Boolean,
    val error: String? = null
)
```

#### ReceiptData
Contains structured receipt information:
```kotlin
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
```

### 4. ViewModel Integration

#### PhotoPreviewViewModel
Enhanced to include ML Kit processing:
```kotlin
// Process receipt image
fun processReceiptImage(bitmap: Bitmap)
fun processReceiptImageFromUri(uri: Uri)

// Updated UI state
data class PhotoPreviewUiState(
    val isProcessing: Boolean = false,
    val receiptData: ReceiptData? = null,
    val extractedText: String? = null,
    // ... other fields
)
```

### 5. Usage Example

#### Basic Text Recognition
```kotlin
// Inject the use case
@Inject
lateinit var processReceiptImageUseCase: ProcessReceiptImageUseCase

// Process receipt image
processReceiptImageUseCase.processReceiptFromBitmap(bitmap)
    .collect { result ->
        when (result) {
            is UiState.Loading -> {
                // Show loading state
            }
            is UiState.Success -> {
                val receiptData = result.data
                // Use extracted receipt data
            }
            is UiState.Error -> {
                // Handle error
            }
        }
    }
```

#### In Compose UI
```kotlin
@Composable
fun ReceiptProcessingScreen(
    viewModel: PhotoPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Process button
    Button(
        onClick = { viewModel.processReceiptImage(bitmap) },
        enabled = !uiState.isProcessing
    ) {
        if (uiState.isProcessing) {
            CircularProgressIndicator()
        } else {
            Text("Process Receipt")
        }
    }
    
    // Display results
    uiState.receiptData?.let { data ->
        Column {
            Text("Merchant: ${data.merchantName ?: "Not found"}")
            Text("Total: $${data.total ?: "Not found"}")
            Text("Items: ${data.items.size}")
        }
    }
}
```

### 6. Receipt Parsing Features

#### Merchant Detection
- Recognizes common merchant names (Walmart, Target, Starbucks, etc.)
- Identifies merchant names from uppercase text patterns
- Extracts merchant address information

#### Financial Information
- Total amount extraction with regex patterns
- Subtotal and tax amount detection
- Individual item pricing

#### Date/Time Extraction
- Various date formats (MM/DD/YYYY, DD-MM-YYYY, etc.)
- Time parsing with AM/PM support

#### Item Parsing
- Identifies individual receipt items
- Extracts item names and prices
- Filters out non-item lines (totals, headers, etc.)

### 7. Error Handling

The implementation includes comprehensive error handling:
- ML Kit processing failures
- Image loading errors
- Text parsing exceptions
- Network-related issues

### 8. Testing

A test activity is provided (`TestMLKitActivity`) to verify ML Kit functionality:
- Image selection from gallery
- Real-time text recognition
- Receipt data extraction display
- Error handling demonstration

### 9. Performance Considerations

- **Offline Processing**: ML Kit works offline, no internet required
- **Image Size**: Optimize images for better recognition accuracy
- **Memory Management**: Properly dispose of bitmaps after processing
- **Threading**: Processing is handled on background threads

### 10. Future Enhancements

Potential improvements:
- Custom model training for better receipt recognition
- OCR result confidence scoring
- Receipt template matching
- Multi-language support
- Barcode/QR code detection

### 11. Dependencies in DI Module

```kotlin
@Provides
@Singleton
fun provideTextRecognitionService(@ApplicationContext context: Context): TextRecognitionService {
    return TextRecognitionService(context)
}

@Provides
@Singleton
fun provideReceiptParserService(): ReceiptParserService {
    return ReceiptParserService()
}
```

## Integration Status
✅ **ML Kit Text Recognition v2 - IMPLEMENTED**
✅ **Receipt Parser Service - IMPLEMENTED**  
✅ **Use Case Layer - IMPLEMENTED**
✅ **ViewModel Integration - IMPLEMENTED**
✅ **Error Handling - IMPLEMENTED**
✅ **Testing Support - IMPLEMENTED**

The ML Kit integration is complete and ready for use in your receipt scanning functionality.
