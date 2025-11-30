package com.example.myapplication.Notification;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.myapplication.AmazonBedrockService.BedrockWarningEngine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Random;

public class GroupAlertWorker extends Worker {
    
    public GroupAlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        sendGroupAlertToAllUsers();
        return Result.success();
    }
    
    private void sendGroupAlertToAllUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> messages = BedrockWarningEngine.getWarningMessages();
                String alertMessage = messages.get(new Random().nextInt(messages.size()));
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String fcmToken = userSnapshot.child("fcmToken").getValue(String.class);
                    
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        SendNotification notification = new SendNotification(
                            fcmToken,
                            "🏆 Group Performance Alert",
                            alertMessage,
                            getApplicationContext()
                        );
                        notification.SendNotifications();
                        Log.d("GroupAlertWorker", "Sent alert to user: " + userSnapshot.getKey());
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupAlertWorker", "Failed to fetch users: " + error.getMessage());
            }
        });
    }
}
