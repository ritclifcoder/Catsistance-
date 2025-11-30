package com.example.myapplication.Notification;

import android.content.Context;
import android.util.Log;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class NotificationSchedulerFixed {
    
    private static final String TAG = "NotifScheduler";
    private static final String WORK_NAME = "group_alerts";
    
    // Use WorkManager for reliable background execution
    public static void scheduleGroupAlerts(Context context) {
        Log.d(TAG, "=== SCHEDULING NOTIFICATIONS ===");
        
        // Create periodic work request (minimum 15 minutes for WorkManager)
        // For 2-minute testing, use Handler in foreground or reduce to 15 min
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            GroupAlertWorker.class,
            15, // Minimum interval for WorkManager
            TimeUnit.MINUTES
        ).build();
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        );
        
        Log.d(TAG, "✅ WorkManager scheduled: every 15 minutes");
        Log.d(TAG, "Work ID: " + workRequest.getId());
    }
    
    public static void cancelGroupAlerts(Context context) {
        Log.d(TAG, "=== CANCELLING NOTIFICATIONS ===");
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        Log.d(TAG, "✅ WorkManager cancelled");
    }
}
