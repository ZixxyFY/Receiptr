package com.receiptr.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.receiptr.data.cache.CacheDao
import com.receiptr.data.cache.CacheMetadata
import android.content.Context
import android.util.Log

@Database(
    entities = [ReceiptEntity::class, CacheMetadata::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun cacheDao(): CacheDao
    
    companion object {
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null
        
        // Migration from version 1 to 2: Add cache_metadata table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("ReceiptDatabase", "Running migration from version 1 to 2")
                try {
                    database.execSQL(
                        """CREATE TABLE IF NOT EXISTS `cache_metadata` (
                            `key` TEXT NOT NULL,
                            `lastUpdated` INTEGER NOT NULL,
                            `expirationTime` INTEGER NOT NULL,
                            `version` INTEGER NOT NULL,
                            PRIMARY KEY(`key`)
                        )"""
                    )
                    Log.d("ReceiptDatabase", "Migration from version 1 to 2 completed successfully")
                } catch (e: Exception) {
                    Log.e("ReceiptDatabase", "Migration from version 1 to 2 failed", e)
                    throw e
                }
            }
        }

        fun getDatabase(context: Context): ReceiptDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReceiptDatabase::class.java,
                    "receipt_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade()
                .fallbackToDestructiveMigrationFrom(1) // Specifically handle version 1
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
