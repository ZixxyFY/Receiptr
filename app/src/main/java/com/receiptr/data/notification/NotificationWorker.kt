package com.receiptr.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.receiptr.domain.repository.ReceiptRepository
import com.receiptr.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: NotificationManager,
    private val receiptRepository: ReceiptRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
            val userId = currentUser?.id ?: return Result.failure()
            
            val receipts = receiptRepository.getAllReceipts(userId).first()
            val weeklyTotal = receipts
                .filter { receipt ->
                    val now = System.currentTimeMillis()
                    val weekAgo = now - TimeUnit.DAYS.toMillis(7)
                    receipt.date >= weekAgo
                }
                .sumOf { it.totalAmount }
            
            if (weeklyTotal > 0) {
                notificationManager.sendWeeklySummaryNotification(weeklyTotal)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "weekly_summary_work"
        
        fun scheduleWeeklySummary(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

@HiltWorker
class ScanReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationManager: NotificationManager,
    private val receiptRepository: ReceiptRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
            val userId = currentUser?.id ?: return Result.failure()
            
            val receipts = receiptRepository.getAllReceipts(userId).first()
            val lastReceiptTime = receipts.maxByOrNull { it.createdAt }?.createdAt ?: 0
            val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
            
            // Send reminder if no receipts in the last 3 days
            if (lastReceiptTime < threeDaysAgo) {
                notificationManager.sendReminderScanNotification()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "scan_reminder_work"
        
        fun scheduleScanReminder(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScanReminderWorker>(3, TimeUnit.DAYS)
                .setInitialDelay(3, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
