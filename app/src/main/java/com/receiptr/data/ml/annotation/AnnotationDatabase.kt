package com.receiptr.data.ml.annotation

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.receiptr.data.ml.schema.ReceiptSchemaConverters

/**
 * Room database for annotation and training data management
 */
@Database(
    entities = [
        AnnotationTaskEntity::class,
        TrainingDataEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    AnnotationConverters::class,
    ReceiptSchemaConverters::class
)
abstract class AnnotationDatabase : RoomDatabase() {
    abstract fun annotationDao(): AnnotationDao
    
    companion object {
        const val DATABASE_NAME = "annotation_database"
    }
}
