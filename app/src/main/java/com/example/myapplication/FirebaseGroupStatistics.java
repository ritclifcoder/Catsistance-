package com.example.myapplication;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class FirebaseGroupStatistics {
    
    static class GroupData {
        int userCount = 0;
        int totalPoints = 0;
        int totalSteps = 0;
        int totalSystolic = 0;
        int totalDiastolic = 0;
        int totalHeartRate = 0;
    }
    
    public static void calculateGroupAverages(Context context) {
        android.util.Log.d("groupspointsfire", "Starting Firebase query...");
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    android.util.Log.d("groupspointsfire", "Firebase callback received, users: " + snapshot.getChildrenCount());
                    Map<String, GroupData> groups = new HashMap<>();
                    groups.put("Silver", new GroupData());
                    groups.put("Gold", new GroupData());
                    groups.put("Master", new GroupData());
                    groups.put("Elite", new GroupData());
                    
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String group = userSnapshot.child("group").getValue(String.class);
                        if (group != null && groups.containsKey(group)) {
                            GroupData data = groups.get(group);
                            data.userCount++;
                            
                            Integer points = userSnapshot.child("points").getValue(Integer.class);
                            if (points != null) data.totalPoints += points;
                            
                            Integer steps = userSnapshot.child("healthStats").child("steps").getValue(Integer.class);
                            if (steps != null) data.totalSteps += steps;
                            
                            Integer systolic = userSnapshot.child("vitals").child("systolic").getValue(Integer.class);
                            Integer diastolic = userSnapshot.child("vitals").child("diastolic").getValue(Integer.class);
                            Integer heartRate = userSnapshot.child("vitals").child("heartRate").getValue(Integer.class);
                            if (systolic != null) data.totalSystolic += systolic;
                            if (diastolic != null) data.totalDiastolic += diastolic;
                            if (heartRate != null) data.totalHeartRate += heartRate;
                        }
                    }
                    
                    android.util.Log.d("groupspointsfire", "Processing results...");
                    for (Map.Entry<String, GroupData> entry : groups.entrySet()) {
                        GroupData data = entry.getValue();
                        if (data.userCount > 0) {
                            double avgSystolic = (double) data.totalSystolic / data.userCount;
                            double avgDiastolic = (double) data.totalDiastolic / data.userCount;
                            double avgHeartRate = (double) data.totalHeartRate / data.userCount;
                            
                            android.util.Log.d("groupspointsfire", entry.getKey() + " - Systolic: " + String.format("%.2f", avgSystolic) + ", Diastolic: " + String.format("%.2f", avgDiastolic) + ", HeartRate: " + String.format("%.2f", avgHeartRate));
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("groupspointsfire", "Error: " + error.getMessage());
                }
            });
    }
}
