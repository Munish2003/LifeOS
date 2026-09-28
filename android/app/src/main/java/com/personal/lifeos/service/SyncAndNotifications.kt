package com.personal.lifeos.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationHelper(private val context: Context) {
    companion object {
        const val REMINDER_CHANNEL_ID = "lifeos_reminders"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Life OS Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Smart reminders for step targets, tasks, and evening check-ins"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminder(title: String, message: String, notificationId: Int = 2001) {
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Background periodic sync logic with FastAPI backend
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
