package com.receiptr.data.ml.annotation

import androidx.room.*
import com.receiptr.data.ml.schema.ReceiptSchema
import com.google.gson.Gson
import java.util.*

/**
 * Repository interface for annotation and training data management
 */
interface AnnotationRepository {
    suspend fun insertAnnotationTask(task: AnnotationTask)
    suspend fun updateAnnotationTask(task: AnnotationTask)
    suspend fun getAnnotationTask(taskId: String): AnnotationTask?
    suspend fun getPendingTasks(limit: Int, priority: AnnotationPriority?): List<AnnotationTask>
    suspend fun getPendingTasksCount(): Int
    suspend fun getCompletedTasksCount(): Int
    
    suspend fun insertTrainingDataEntry(entry: TrainingDataEntry)
    suspend fun getTrainingDataStats(): TrainingDataStatsEntity
    suspend fun getTotalTrainingEntries(): Int
    suspend fun getTrainingDataByCategory(category: String): List<TrainingDataEntry>
    suspend fun getTrainingDataByDateRange(startDate: Date, endDate: Date): List<TrainingDataEntry>
}

/**
 * Room database implementation
 */
@Dao
interface AnnotationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotationTask(task: AnnotationTaskEntity)
    
    @Update
    suspend fun updateAnnotationTask(task: AnnotationTaskEntity)
    
    @Query("SELECT * FROM annotation_tasks WHERE id = :taskId")
    suspend fun getAnnotationTask(taskId: String): AnnotationTaskEntity?
    
    @Query("SELECT * FROM annotation_tasks WHERE status = :status ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    suspend fun getPendingTasks(status: String = "PENDING", limit: Int): List<AnnotationTaskEntity>
    
    @Query("SELECT * FROM annotation_tasks WHERE status = :status AND priority = :priority ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingTasksByPriority(status: String = "PENDING", priority: String, limit: Int): List<AnnotationTaskEntity>
    
    @Query("SELECT COUNT(*) FROM annotation_tasks WHERE status = 'PENDING'")
    suspend fun getPendingTasksCount(): Int
    
    @Query("SELECT COUNT(*) FROM annotation_tasks WHERE status = 'COMPLETED'")
    suspend fun getCompletedTasksCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingDataEntry(entry: TrainingDataEntryEntity)
    
    @Query("SELECT COUNT(*) as total_entries FROM training_data_entries")
    suspend fun getTotalTrainingEntries(): Int
    
    @Query("SELECT * FROM training_data_entries WHERE correctedDataCategory = :category")
    suspend fun getTrainingDataByCategory(category: String): List<TrainingDataEntryEntity>
    
    @Query("SELECT * FROM training_data_entries WHERE createdAt BETWEEN :startDate AND :endDate")
    suspend fun getTrainingDataByDateRange(startDate: Long, endDate: Long): List<TrainingDataEntryEntity>
    
    @Query("""
        SELECT 
            COUNT(*) as totalEntries,
            AVG(correctedDataConfidence) as averageConfidence,
            MAX(createdAt) as lastUpdated
        FROM training_data_entries
    """)
    suspend fun getTrainingDataStats(): TrainingDataStatsEntity
    
    @Query("""
        SELECT 
            correctedDataCategory as category,
            COUNT(*) as count
        FROM training_data_entries 
        GROUP BY correctedDataCategory
    """)
    suspend fun getEntriesByCategory(): List<CategoryStatsEntity>
    
    @Query("""
        SELECT 
            CASE 
                WHEN correctedDataConfidence >= 0.8 THEN 'HIGH'
                WHEN correctedDataConfidence >= 0.6 THEN 'MEDIUM'
                ELSE 'LOW'
            END as confidenceLevel,
            COUNT(*) as count
        FROM training_data_entries 
        GROUP BY confidenceLevel
    """)
    suspend fun getEntriesByConfidence(): List<ConfidenceStatsEntity>
}

/**
 * Room entities for persistence
 */
@Entity(tableName = "annotation_tasks")
@TypeConverters(AnnotationConverters::class)
data class AnnotationTaskEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val originalData: ReceiptSchema,
    val correctedData: ReceiptSchema?,
    val imageUri: String?,
    val validationResult: String, // JSON serialized ValidationResult
    val priority: String,
    val status: String,
    val createdAt: Long,
    val assignedTo: String?,
    val completedAt: Long?,
    val notes: String?
)

@Entity(tableName = "training_data_entries")
@TypeConverters(AnnotationConverters::class)
data class TrainingDataEntryEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val originalData: ReceiptSchema,
    val correctedData: ReceiptSchema,
    val correctedDataCategory: String,
    val correctedDataConfidence: Float,
    val annotatorId: String?,
    val annotationNotes: String?,
    val validationResult: String, // JSON serialized ValidationResult
    val createdAt: Long
)

data class TrainingDataStatsEntity(
    val totalEntries: Int,
    val averageConfidence: Float,
    val lastUpdated: Long
)

data class CategoryStatsEntity(
    val category: String,
    val count: Int
)

data class ConfidenceStatsEntity(
    val confidenceLevel: String,
    val count: Int
)

/**
 * Type converters for Room
 */
class AnnotationConverters {
    
    @TypeConverter
    fun fromReceiptSchema(schema: ReceiptSchema?): String? {
        return schema?.let { Gson().toJson(it) }
    }
    
    @TypeConverter
    fun toReceiptSchema(json: String?): ReceiptSchema? {
        return json?.let { Gson().fromJson(it, ReceiptSchema::class.java) }
    }
}

/**
 * Repository implementation
 */
class AnnotationRepositoryImpl(
    private val annotationDao: AnnotationDao
) : AnnotationRepository {
    
    override suspend fun insertAnnotationTask(task: AnnotationTask) {
        annotationDao.insertAnnotationTask(task.toEntity())
    }
    
    override suspend fun updateAnnotationTask(task: AnnotationTask) {
        annotationDao.updateAnnotationTask(task.toEntity())
    }
    
    override suspend fun getAnnotationTask(taskId: String): AnnotationTask? {
        return annotationDao.getAnnotationTask(taskId)?.toDomainModel()
    }
    
    override suspend fun getPendingTasks(limit: Int, priority: AnnotationPriority?): List<AnnotationTask> {
        return if (priority != null) {
            annotationDao.getPendingTasksByPriority("PENDING", priority.name, limit)
        } else {
            annotationDao.getPendingTasks("PENDING", limit)
        }.map { it.toDomainModel() }
    }
    
    override suspend fun getPendingTasksCount(): Int {
        return annotationDao.getPendingTasksCount()
    }
    
    override suspend fun getCompletedTasksCount(): Int {
        return annotationDao.getCompletedTasksCount()
    }
    
    override suspend fun insertTrainingDataEntry(entry: TrainingDataEntry) {
        annotationDao.insertTrainingDataEntry(entry.toEntity())
    }
    
    override suspend fun getTrainingDataStats(): TrainingDataStatsEntity {
        val baseStats = annotationDao.getTrainingDataStats()
        val categoryStats = annotationDao.getEntriesByCategory()
        val confidenceStats = annotationDao.getEntriesByConfidence()
        
        return TrainingDataStatsEntity(
            totalEntries = baseStats.totalEntries,
            averageConfidence = baseStats.averageConfidence,
            lastUpdated = baseStats.lastUpdated
        )
    }
    
    override suspend fun getTotalTrainingEntries(): Int {
        return annotationDao.getTotalTrainingEntries()
    }
    
    override suspend fun getTrainingDataByCategory(category: String): List<TrainingDataEntry> {
        return annotationDao.getTrainingDataByCategory(category).map { it.toDomainModel() }
    }
    
    override suspend fun getTrainingDataByDateRange(startDate: Date, endDate: Date): List<TrainingDataEntry> {
        return annotationDao.getTrainingDataByDateRange(startDate.time, endDate.time).map { it.toDomainModel() }
    }
}

/**
 * Extension functions for entity conversion
 */
fun AnnotationTask.toEntity(): AnnotationTaskEntity {
    return AnnotationTaskEntity(
        id = id,
        receiptId = receiptId,
        originalData = originalData,
        correctedData = correctedData,
        imageUri = imageUri,
        validationResult = Gson().toJson(validationResult),
        priority = priority.name,
        status = status.name,
        createdAt = createdAt.time,
        assignedTo = assignedTo,
        completedAt = completedAt?.time,
        notes = notes
    )
}

fun AnnotationTaskEntity.toDomainModel(): AnnotationTask {
    return AnnotationTask(
        id = id,
        receiptId = receiptId,
        originalData = originalData,
        correctedData = correctedData,
        imageUri = imageUri,
        validationResult = Gson().fromJson(validationResult, com.receiptr.data.ml.schema.ValidationResult::class.java),
        priority = AnnotationPriority.valueOf(priority),
        status = AnnotationStatus.valueOf(status),
        createdAt = Date(createdAt),
        assignedTo = assignedTo,
        completedAt = completedAt?.let { Date(it) },
        notes = notes
    )
}

fun TrainingDataEntry.toEntity(): TrainingDataEntryEntity {
    return TrainingDataEntryEntity(
        id = id,
        receiptId = receiptId,
        originalData = originalData,
        correctedData = correctedData,
        correctedDataCategory = correctedData.category.name,
        correctedDataConfidence = correctedData.confidence,
        annotatorId = annotatorId,
        annotationNotes = annotationNotes,
        validationResult = Gson().toJson(validationResult),
        createdAt = createdAt.time
    )
}

fun TrainingDataEntryEntity.toDomainModel(): TrainingDataEntry {
    return TrainingDataEntry(
        id = id,
        receiptId = receiptId,
        originalData = originalData,
        correctedData = correctedData,
        annotatorId = annotatorId,
        annotationNotes = annotationNotes,
        validationResult = Gson().fromJson(validationResult, com.receiptr.data.ml.schema.ValidationResult::class.java),
        createdAt = Date(createdAt)
    )
}
