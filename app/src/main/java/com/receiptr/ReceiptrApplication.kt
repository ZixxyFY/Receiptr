package com.receiptr

import android.app.Application
import com.receiptr.data.notification.WeeklySummaryWorker
import com.receiptr.data.notification.ScanReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReceiptrApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification workers
        WeeklySummaryWorker.scheduleWeeklySummary(this)
        ScanReminderWorker.scheduleScanReminder(this)
    }
}
