package com.personal.lifeos.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Manages bi-directional integration with Google Fit and Android Health Connect.
 * Health Connect is Google's official Android platform for syncing steps, distance,
 * calories, and workouts directly from Google Fit, WearOS smartwatches, and fitness trackers.
 */
class GoogleFitManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lifeos_google_fit_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "GoogleFitManager"
        const val PREF_CONNECTED = "google_fit_connected"
        const val PREF_LAST_SYNC_TIME = "last_sync_time"
        const val PREF_LAST_SYNC_STEPS = "last_sync_steps"
        const val PREF_AUTO_SYNC = "auto_sync_enabled"

        const val GOOGLE_FIT_PACKAGE = "com.google.android.apps.fitness"
        const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class)
        )
    }

    val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (isHealthConnectAvailable()) {
                HealthConnectClient.getOrCreate(context)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing HealthConnectClient: ${e.message}")
            null
        }
    }

    fun isHealthConnectAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            status == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect check failed: ${e.message}")
            false
        }
    }

    fun isGoogleFitInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(GOOGLE_FIT_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isConnected(): Boolean {
        return prefs.getBoolean(PREF_CONNECTED, false)
    }

    fun setConnected(connected: Boolean) {
        prefs.edit().putBoolean(PREF_CONNECTED, connected).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(PREF_LAST_SYNC_TIME, 0L)
    }

    fun getLastSyncSteps(): Int {
        return prefs.getInt(PREF_LAST_SYNC_STEPS, 0)
    }

    fun isAutoSync(): Boolean {
        return prefs.getBoolean(PREF_AUTO_SYNC, true)
    }

    fun setAutoSync(auto: Boolean) {
        prefs.edit().putBoolean(PREF_AUTO_SYNC, auto).apply()
    }

    suspend fun hasPermissions(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = healthConnectClient ?: return@withContext false
                val granted = client.permissionController.getGrantedPermissions()
                granted.containsAll(PERMISSIONS)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking health permissions: ${e.message}")
                false
            }
        }
    }

    /**
     * Reads steps recorded today between midnight and now from Google Fit / Health Connect.
     */
    suspend fun readTodaySteps(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val client = healthConnectClient ?: return@withContext 0L
                val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endTime = Instant.now()

                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                val totalSteps = response.records.sumOf { it.count }
                if (totalSteps > 0) {
                    prefs.edit()
                        .putLong(PREF_LAST_SYNC_TIME, System.currentTimeMillis())
                        .putInt(PREF_LAST_SYNC_STEPS, totalSteps.toInt())
                        .apply()
                }
                totalSteps
            } catch (e: Exception) {
                Log.e(TAG, "Error reading steps: ${e.message}")
                0L
            }
        }
    }

    /**
     * Reads distance covered today in kilometers.
     */
    suspend fun readTodayDistance(): Double {
        return withContext(Dispatchers.IO) {
            try {
                val client = healthConnectClient ?: return@withContext 0.0
                val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endTime = Instant.now()

                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = DistanceRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                response.records.sumOf { it.distance.inMeters } / 1000.0
            } catch (e: Exception) {
                0.0
            }
        }
    }

    /**
     * Reads calories burned today in kcal.
     */
    suspend fun readTodayCalories(): Double {
        return withContext(Dispatchers.IO) {
            try {
                val client = healthConnectClient ?: return@withContext 0.0
                val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endTime = Instant.now()

                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = TotalCaloriesBurnedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                response.records.sumOf { it.energy.inKilocalories }
            } catch (e: Exception) {
                0.0
            }                                     
        }
    }

    /**
     * Reads latest logged weight in kg.
     */
    suspend fun readLatestWeight(): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val client = healthConnectClient ?: return@withContext null
                val startTime = Instant.now().minus(90, ChronoUnit.DAYS)
                val endTime = Instant.now()

                val response = client.readRecords(
                    ReadRecordsRequest(
                        recordType = WeightRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                response.records.lastOrNull()?.weight?.inKilograms
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Opens the Google Fit application or redirects to Google Play Store to install it.
     */
    fun openGoogleFitApp() {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(GOOGLE_FIT_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$GOOGLE_FIT_PACKAGE")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Google Fit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the Health Connect settings screen or Play Store page.
     */
    fun openHealthConnectSettings() {
        try {
            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open Health Connect settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
