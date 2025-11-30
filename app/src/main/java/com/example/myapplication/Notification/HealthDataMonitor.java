package com.example.myapplication.Notification;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HealthDataMonitor {
    
    private static final String TAG = "HealthDataMonitor";
    private Context context;
    private String userId;
    private DatabaseReference userHealthRef;
    
    public HealthDataMonitor(Context context) {
        this.context = context;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.userHealthRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId);
    }
    
    public void startMonitoring() {
        Log.d(TAG, "Starting health data monitoring for user: " + userId);
        
        // Monitor vitals (blood pressure, heart rate)
        userHealthRef.child("vitals").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer systolic = snapshot.child("systolic").getValue(Integer.class);
                    Integer diastolic = snapshot.child("diastolic").getValue(Integer.class);
                    
                    if (systolic != null && diastolic != null) {
                        Log.d(TAG, "Blood pressure changed: " + systolic + "/" + diastolic);
                        sendHealthUpdateNotification("Blood Pressure Updated", 
                            "Your BP is now " + systolic + "/" + diastolic);
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Vitals monitoring error: " + error.getMessage());
            }
        });
        
        // Monitor health stats (steps, sleep, weight)
        userHealthRef.child("healthStats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer steps = snapshot.child("steps").getValue(Integer.class);
                    Double sleepHours = snapshot.child("sleepHours").getValue(Double.class);
                    Double weight = snapshot.child("weight").getValue(Double.class);
                    
                    if (steps != null) {
                        Log.d(TAG, "Steps changed: " + steps);
                        sendHealthUpdateNotification("Steps Updated", 
                            "You've walked " + steps + " steps today! 🚶");
                    }
                    
                    if (sleepHours != null) {
                        Log.d(TAG, "Sleep changed: " + sleepHours);
                        sendHealthUpdateNotification("Sleep Data Updated", 
                            "Sleep duration: " + sleepHours + " hours 😴");
                    }
                    
                    if (weight != null) {
                        Log.d(TAG, "Weight changed: " + weight);
                        sendHealthUpdateNotification("Weight Updated", 
                            "Current weight: " + weight + " kg ⚖️");
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Health stats monitoring error: " + error.getMessage());
            }
        });
    }
    
    private void sendHealthUpdateNotification(String title, String message) {
        userHealthRef.child("fcmToken").get().addOnSuccessListener(snapshot -> {
            String fcmToken = snapshot.getValue(String.class);
            if (fcmToken != null && !fcmToken.isEmpty()) {
                SendNotification notification = new SendNotification(
                    fcmToken,
                    "📊 " + title,
                    message,
                    context
                );
                notification.SendNotifications();
                Log.d(TAG, "Sent notification: " + title);
            }
        });
    }
}
