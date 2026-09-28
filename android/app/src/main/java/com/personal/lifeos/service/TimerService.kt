package com.personal.lifeos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.personal.lifeos.MainActivity
import com.personal.lifeos.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isRunning = false
    private var elapsedSeconds = 0L
    private var currentTaskTitle = "Focus Session"
    private var timerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "lifeos_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"

        private val _timerState = MutableStateFlow(0L)
        val timerState = _timerState.asStateFlow()

        private val _isRunningState = MutableStateFlow(false)
        val isRunningState = _isRunningState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        currentTaskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: currentTaskTitle

        when (action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }

        return START_STICKY
    }

    private fun startTimer() {
        if (isRunning) return
        isRunning = true
        _isRunningState.value = true

        startForeground(NOTIFICATION_ID, buildNotification())

        timerJob = serviceScope.launch {
            while (isActive && isRunning) {
                delay(1000)
                elapsedSeconds++
                _timerState.value = elapsedSeconds
                updateNotification()
            }
        }
    }

    private fun pauseTimer() {
        isRunning = false
        _isRunningState.value = false
        timerJob?.cancel()
        updateNotification()
    }

    private fun stopTimer() {
        isRunning = false
        _isRunningState.value = false
        timerJob?.cancel()
        elapsedSeconds = 0L
        _timerState.value = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus: $currentTaskTitle")
            .setContentText(formatTime(elapsedSeconds))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
