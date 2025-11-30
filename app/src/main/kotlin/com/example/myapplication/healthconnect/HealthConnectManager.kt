package com.example.myapplication.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager private constructor(context: Context) {
    
    private val healthConnectClient: HealthConnectClient? = 
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else null
    
    val isAvailable: Boolean
        get() = healthConnectClient != null
    
    fun getTodaySteps(callback: HealthDataCallback) {
        if (healthConnectClient == null) {
            callback.onError("Health Connect not available")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(1, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                
                val response = healthConnectClient.readRecords(request)
                val totalSteps = response.records.sumOf { it.count.toInt() }
                callback.onStepsReceived(totalSteps)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unknown error")
            }
        }
    }
    
    fun getTodayBloodPressure(callback: HealthDataCallback) {
        if (healthConnectClient == null) {
            callback.onError("Health Connect not available")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(1, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                
                val response = healthConnectClient.readRecords(request)
                if (response.records.isNotEmpty()) {
                    val latest = response.records.first()
                    val systolic = latest.systolic.inMillimetersOfMercury.toInt()
                    val diastolic = latest.diastolic.inMillimetersOfMercury.toInt()
                    callback.onBloodPressureReceived(systolic, diastolic)
                } else {
                    callback.onError("No blood pressure data")
                }
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unknown error")
            }
        }
    }
    
    fun getTodaySleep(callback: HealthDataCallback) {
        if (healthConnectClient == null) {
            callback.onError("Health Connect not available")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minus(1, ChronoUnit.DAYS)
                
                val request = ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
                
                val response = healthConnectClient.readRecords(request)
                val totalMinutes = response.records.sumOf { 
                    ChronoUnit.MINUTES.between(it.startTime, it.endTime)
                }
                val hours = (totalMinutes / 60).toInt()
                val minutes = (totalMinutes % 60).toInt()
                callback.onSleepReceived(hours, minutes)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unknown error")
            }
        }
    }
    
    interface HealthDataCallback {
        fun onStepsReceived(steps: Int) {}
        fun onBloodPressureReceived(systolic: Int, diastolic: Int) {}
        fun onSleepReceived(hours: Int, minutes: Int) {}
        fun onError(error: String)
    }
    
    companion object {
        @Volatile
        private var instance: HealthConnectManager? = null
        
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class)
        )
        
        fun getInstance(context: Context): HealthConnectManager {
            return instance ?: synchronized(this) {
                instance ?: HealthConnectManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
