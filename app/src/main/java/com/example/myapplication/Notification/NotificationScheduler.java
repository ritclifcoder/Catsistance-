package com.example.myapplication.Notification;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.myapplication.AmazonBedrockService.BedrockWarningEngine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import java.util.Random;

public class NotificationScheduler {
    
    private static Handler handler;
    private static Runnable notificationRunnable;
    
    // Change interval: 30 seconds for testing, 2 minutes for demo, 180 minutes (3 hours) for production
    private static final long INTERVAL_MILLIS = 30 * 1000; // 30 seconds for testing
    
    public static void scheduleGroupAlerts(Context context) {
        Log.d("NotificationScheduler", "\n\n========================================");
        Log.d("NotificationScheduler", "INITIALIZING NOTIFICATION SCHEDULER");
        Log.d("NotificationScheduler", "========================================");
        Log.d("NotificationScheduler", "Interval: " + (INTERVAL_MILLIS / 1000) + " seconds (" + (INTERVAL_MILLIS / 60000) + " minutes)");
        Log.d("NotificationScheduler", "Context: " + context.getClass().getSimpleName());
        
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
            Log.d("NotificationScheduler", "✅ Handler created on main looper");
        } else {
            Log.d("NotificationScheduler", "⚠️ Handler already exists, reusing");
        }
        
        notificationRunnable = new Runnable() {
            private int executionCount = 0;
            
            @Override
            public void run() {
                executionCount++;
                Log.d("NotificationScheduler", "\n--- EXECUTION #" + executionCount + " ---");
                Log.d("NotificationScheduler", "Time: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
                Log.d("NotificationScheduler", "Thread: " + Thread.currentThread().getName());
                
                sendGroupAlertToAllUsers(context);
                
                Log.d("NotificationScheduler", "Scheduling next execution in " + (INTERVAL_MILLIS / 1000) + " seconds...");
                handler.postDelayed(this, INTERVAL_MILLIS);
            }
        };
        
        // Start scheduling - first notification after 2 minutes
        handler.postDelayed(notificationRunnable, INTERVAL_MILLIS);
        Log.d("NotificationScheduler", "✅ Scheduler active: First sent now, next in 2 minutes");
        Log.d("NotificationScheduler", "========================================\n");
    }
    
    private static void saveNotificationToFirebase(String userId, String title, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
            .getReference("notifications")
            .child(userId)
            .push();
        
        java.util.HashMap<String, Object> notifData = new java.util.HashMap<>();
        notifData.put("title", title);
        notifData.put("message", message);
        notifData.put("timestamp", System.currentTimeMillis());
        
        notifRef.setValue(notifData);
    }
    
    public static void cancelGroupAlerts() {
        Log.d("NotificationScheduler", "\n========================================");
        Log.d("NotificationScheduler", "CANCELLING NOTIFICATION SCHEDULER");
        Log.d("NotificationScheduler", "========================================");
        
        if (handler != null && notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
            Log.d("NotificationScheduler", "✅ Handler callbacks removed");
        } else {
            Log.w("NotificationScheduler", "⚠️ Handler or runnable was null");
        }
        
        Log.d("NotificationScheduler", "========================================\n");
    }
    
    private static void sendGroupAlertToAllUsers(Context context) {
        Log.d("NotificationScheduler", "=== STARTING NOTIFICATION BATCH ===");
        Log.d("NotificationScheduler", "Timestamp: " + System.currentTimeMillis());
        
        // Ensure Firebase is authenticated
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("NotificationScheduler", "❌ User not authenticated - cannot read database");
            return;
        }
        
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("NotificationScheduler", "✅ Firebase query successful");
                Log.d("NotificationScheduler", "Total users in database: " + snapshot.getChildrenCount());
                
                List<String> messages = BedrockWarningEngine.getWarningMessages();
                String alertMessage = messages.get(new Random().nextInt(messages.size()));
                Log.d("NotificationScheduler", "Selected message: " + alertMessage);
                
                int sentCount = 0;
                int skippedCount = 0;
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String fcmToken = userSnapshot.child("fcmToken").getValue(String.class);
                    
                    Log.d("NotificationScheduler", "Processing user: " + userId);
                    Log.d("NotificationScheduler", "  FCM Token: " + (fcmToken != null ? fcmToken.substring(0, Math.min(20, fcmToken.length())) + "..." : "NULL"));
                    
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        String title = "🏆 Group Performance Alert";
                        SendNotification notification = new SendNotification(
                            fcmToken,
                            title,
                            alertMessage,
                            context
                        );
                        notification.SendNotifications();
                        
                        // Save to Firebase
                        saveNotificationToFirebase(userId, title, alertMessage);
                        
                        sentCount++;
                        Log.d("NotificationScheduler", "  ✅ Notification sent to: " + userId);
                    } else {
                        skippedCount++;
                        Log.w("NotificationScheduler", "  ⚠️ Skipped (no token): " + userId);
                    }
                }
                
                Log.d("NotificationScheduler", "=== BATCH COMPLETE ===");
                Log.d("NotificationScheduler", "Sent: " + sentCount + ", Skipped: " + skippedCount);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationScheduler", "❌ Firebase query FAILED");
                Log.e("NotificationScheduler", "Error code: " + error.getCode());
                Log.e("NotificationScheduler", "Error message: " + error.getMessage());
                Log.e("NotificationScheduler", "Error details: " + error.getDetails());
            }
        });
    }
}
