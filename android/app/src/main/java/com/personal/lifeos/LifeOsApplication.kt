package com.personal.lifeos

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.personal.lifeos.service.NotificationHelper
import com.personal.lifeos.service.SyncWorker
import java.util.concurrent.TimeUnit

class LifeOsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels
        NotificationHelper(this)

        // Setup background periodic sync
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "lifeos_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
