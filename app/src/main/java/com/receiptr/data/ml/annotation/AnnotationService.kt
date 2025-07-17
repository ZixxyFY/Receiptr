package com.receiptr.data.ml.annotation

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.receiptr.data.ml.schema.ReceiptSchema
import com.receiptr.data.ml.schema.ValidationResult
import com.receiptr.data.ml.schema.validate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling data annotation, manual correction, and training data accumulation
 */
@Singleton
class AnnotationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val annotationRepository: AnnotationRepository
) {
    
    companion object {
        private const val TAG = "AnnotationService"
        private const val ANNOTATION_DIR = "annotations"
        private const val TRAINING_DATA_DIR = "training_data"
    }
    
    /**
     * Submit receipt data for annotation/correction
     */
    suspend fun submitForAnnotation(
        receiptSchema: ReceiptSchema,
        imageUri: Uri? = null,
        priority: AnnotationPriority = AnnotationPriority.NORMAL
    ): AnnotationTask = withContext(Dispatchers.IO) {
        Log.d(TAG, "Submitting receipt for annotation: ${receiptSchema.id}")
        
        // Validate the data first
        val validationResult = receiptSchema.validate()
        
        // Create annotation task
        val task = AnnotationTask(
            receiptId = receiptSchema.id,
            originalData = receiptSchema,
            correctedData = null,
            imageUri = imageUri?.toString(),
            validationResult = validationResult,
            priority = priority,
            status = AnnotationStatus.PENDING,
            createdAt = Date(),
            assignedTo = null,
            completedAt = null,
            notes = null
        )
        
        // Save to repository
        annotationRepository.insertAnnotationTask(task)
        
        // Export for external annotation tools if needed
        exportForExternalTool(task)
        
        task
    }
    
    /**
     * Get pending annotation tasks
     */
    suspend fun getPendingTasks(
        limit: Int = 50,
        priority: AnnotationPriority? = null
    ): List<AnnotationTask> = withContext(Dispatchers.IO) {
        annotationRepository.getPendingTasks(limit, priority)
    }
    
    /**
     * Complete annotation task with corrected data
     */
    suspend fun completeAnnotation(
        taskId: String,
        correctedData: ReceiptSchema,
        annotatorId: String,
        notes: String? = null
    ): AnnotationTask = withContext(Dispatchers.IO) {
        Log.d(TAG, "Completing annotation for task: $taskId")
        
        val task = annotationRepository.getAnnotationTask(taskId)
            ?: throw IllegalArgumentException("Annotation task not found: $taskId")
        
        // Validate corrected data
        val validationResult = correctedData.validate()
        
        // Update task
        val updatedTask = task.copy(
            correctedData = correctedData.copy(
                isManuallyVerified = true,
                updatedAt = Date()
            ),
            validationResult = validationResult,
            status = AnnotationStatus.COMPLETED,
            assignedTo = annotatorId,
            completedAt = Date(),
            notes = notes
        )
        
        annotationRepository.updateAnnotationTask(updatedTask)
        
        // Add to training dataset
        addToTrainingDataset(updatedTask)
        
        updatedTask
    }
    
    /**
     * Add corrected data to training dataset
     */
    private suspend fun addToTrainingDataset(task: AnnotationTask) {
        val correctedData = task.correctedData ?: return
        
        try {
            // Create training data entry
            val trainingEntry = TrainingDataEntry(
                id = UUID.randomUUID().toString(),
                receiptId = task.receiptId,
                originalData = task.originalData,
                correctedData = correctedData,
                annotatorId = task.assignedTo,
                annotationNotes = task.notes,
                validationResult = task.validationResult,
                createdAt = Date()
            )
            
            // Save to training data repository
            annotationRepository.insertTrainingDataEntry(trainingEntry)
            
            // Export to file for ML training
            exportTrainingData(trainingEntry)
            
            Log.d(TAG, "Added entry to training dataset: ${trainingEntry.id}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to training dataset", e)
        }
    }
    
    /**
     * Export training data to file formats suitable for ML training
     */
    private suspend fun exportTrainingData(entry: TrainingDataEntry) {
        try {
            val trainingDir = File(context.filesDir, TRAINING_DATA_DIR)
            trainingDir.mkdirs()
            
            // Export as JSON for general ML training
            val jsonFile = File(trainingDir, "${entry.id}.json")
            val jsonData = mapOf(
                "id" to entry.id,
                "receipt_id" to entry.receiptId,
                "original_data" to entry.originalData,
                "corrected_data" to entry.correctedData,
                "annotation_notes" to entry.annotationNotes,
                "validation_result" to entry.validationResult,
                "created_at" to entry.createdAt.time
            )
            
            FileWriter(jsonFile).use { writer ->
                writer.write(Gson().toJson(jsonData))
            }
            
            // Export as CSV for specific ML tasks
            exportAsCSV(entry, trainingDir)
            
            // Export as JSONL for NER training
            exportAsJSONL(entry, trainingDir)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting training data", e)
        }
    }
    
    /**
     * Export as CSV format for classification tasks
     */
    private fun exportAsCSV(entry: TrainingDataEntry, trainingDir: File) {
        val csvFile = File(trainingDir, "classification_data.csv")
        val csvData = listOf(
            entry.correctedData.merchantName ?: "",
            entry.correctedData.category.name,
            entry.correctedData.totalAmount?.toString() ?: "",
            entry.correctedData.rawText.replace("\n", " ").take(1000),
            entry.correctedData.lineItems.size.toString(),
            entry.correctedData.paymentMethod?.name ?: ""
        ).joinToString(",")
        
        csvFile.appendText("$csvData\n")
    }
    
    /**
     * Export as JSONL format for NER training
     */
    private fun exportAsJSONL(entry: TrainingDataEntry, trainingDir: File) {
        val jsonlFile = File(trainingDir, "ner_data.jsonl")
        
        val nerData = mapOf(
            "text" to entry.correctedData.rawText,
            "entities" to extractEntities(entry.correctedData),
            "merchant_name" to entry.correctedData.merchantName,
            "total_amount" to entry.correctedData.totalAmount,
            "date" to entry.correctedData.transactionDate?.time,
            "category" to entry.correctedData.category.name
        )
        
        jsonlFile.appendText("${Gson().toJson(nerData)}\n")
    }
    
    /**
     * Extract entities for NER training
     */
    private fun extractEntities(data: ReceiptSchema): List<Map<String, Any>> {
        val entities = mutableListOf<Map<String, Any>>()
        
        // Add merchant name entity
        data.merchantName?.let { name ->
            entities.add(mapOf(
                "text" to name,
                "label" to "MERCHANT_NAME",
                "confidence" to 1.0f
            ))
        }
        
        // Add total amount entity
        data.totalAmount?.let { amount ->
            entities.add(mapOf(
                "text" to amount.toString(),
                "label" to "TOTAL_AMOUNT",
                "confidence" to 1.0f
            ))
        }
        
        // Add date entity
        data.transactionDate?.let { date ->
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            entities.add(mapOf(
                "text" to dateFormat.format(date),
                "label" to "DATE",
                "confidence" to 1.0f
            ))
        }
        
        // Add line item entities
        data.lineItems.forEach { item ->
            entities.add(mapOf(
                "text" to item.name,
                "label" to "ITEM_NAME",
                "confidence" to item.confidence
            ))
            
            entities.add(mapOf(
                "text" to item.totalPrice.toString(),
                "label" to "ITEM_PRICE",
                "confidence" to item.confidence
            ))
        }
        
        return entities
    }
    
    /**
     * Export for external annotation tools (Label Studio, etc.)
     */
    private suspend fun exportForExternalTool(task: AnnotationTask) {
        try {
            val annotationDir = File(context.filesDir, ANNOTATION_DIR)
            annotationDir.mkdirs()
            
            // Export in Label Studio format
            val labelStudioData = mapOf(
                "id" to task.id,
                "data" to mapOf(
                    "text" to task.originalData.rawText,
                    "image" to task.imageUri,
                    "merchant_name" to task.originalData.merchantName,
                    "total_amount" to task.originalData.totalAmount,
                    "date" to task.originalData.transactionDate?.time,
                    "category" to task.originalData.category.name
                ),
                "predictions" to listOf(
                    mapOf(
                        "model_version" to "auto_extraction_v1",
                        "result" to extractAnnotationResults(task.originalData)
                    )
                )
            )
            
            val exportFile = File(annotationDir, "${task.id}_label_studio.json")
            FileWriter(exportFile).use { writer ->
                writer.write(Gson().toJson(labelStudioData))
            }
            
            Log.d(TAG, "Exported annotation task for external tool: ${task.id}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting for external tool", e)
        }
    }
    
    /**
     * Extract annotation results for external tools
     */
    private fun extractAnnotationResults(data: ReceiptSchema): List<Map<String, Any>> {
        val results = mutableListOf<Map<String, Any>>()
        
        // Add text annotations
        data.merchantName?.let { name ->
            results.add(mapOf(
                "from_name" to "merchant_name",
                "to_name" to "text",
                "type" to "text",
                "value" to mapOf(
                    "text" to name,
                    "labels" to listOf("MERCHANT_NAME")
                )
            ))
        }
        
        data.totalAmount?.let { amount ->
            results.add(mapOf(
                "from_name" to "total_amount",
                "to_name" to "text",
                "type" to "text",
                "value" to mapOf(
                    "text" to amount.toString(),
                    "labels" to listOf("TOTAL_AMOUNT")
                )
            ))
        }
        
        return results
    }
    
    /**
     * Get training data statistics
     */
    suspend fun getTrainingDataStats(): TrainingDataStats = withContext(Dispatchers.IO) {
        val stats = annotationRepository.getTrainingDataStats()
        
        TrainingDataStats(
            totalEntries = stats.totalEntries,
            entriesByCategory = emptyMap(), // TODO: Implement category stats
            entriesByConfidence = emptyMap(), // TODO: Implement confidence stats
            averageConfidence = stats.averageConfidence,
            lastUpdated = Date(stats.lastUpdated)
        )
    }
    
    /**
     * Generate annotation report
     */
    suspend fun generateAnnotationReport(): AnnotationReport = withContext(Dispatchers.IO) {
        val pendingTasks = annotationRepository.getPendingTasksCount()
        val completedTasks = annotationRepository.getCompletedTasksCount()
        val totalTrainingEntries = annotationRepository.getTotalTrainingEntries()
        
        AnnotationReport(
            pendingTasks = pendingTasks,
            completedTasks = completedTasks,
            totalTrainingEntries = totalTrainingEntries,
            completionRate = if (pendingTasks + completedTasks > 0) {
                completedTasks.toFloat() / (pendingTasks + completedTasks)
            } else 0f,
            generatedAt = Date()
        )
    }
}

/**
 * Data classes for annotation system
 */
data class AnnotationTask(
    val id: String = UUID.randomUUID().toString(),
    val receiptId: String,
    val originalData: ReceiptSchema,
    val correctedData: ReceiptSchema?,
    val imageUri: String?,
    val validationResult: ValidationResult,
    val priority: AnnotationPriority,
    val status: AnnotationStatus,
    val createdAt: Date,
    val assignedTo: String?,
    val completedAt: Date?,
    val notes: String?
)

data class TrainingDataEntry(
    val id: String,
    val receiptId: String,
    val originalData: ReceiptSchema,
    val correctedData: ReceiptSchema,
    val annotatorId: String?,
    val annotationNotes: String?,
    val validationResult: ValidationResult,
    val createdAt: Date
)

data class TrainingDataStats(
    val totalEntries: Int,
    val entriesByCategory: Map<String, Int>,
    val entriesByConfidence: Map<String, Int>,
    val averageConfidence: Float,
    val lastUpdated: Date
)

data class AnnotationReport(
    val pendingTasks: Int,
    val completedTasks: Int,
    val totalTrainingEntries: Int,
    val completionRate: Float,
    val generatedAt: Date
)

enum class AnnotationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class AnnotationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    ARCHIVED
}
