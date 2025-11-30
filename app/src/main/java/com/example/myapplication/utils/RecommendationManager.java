package com.example.myapplication.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RecommendationManager {
    
    public static void initializeUserRecommendations(String userId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference("users").child(userId).child("recommendationsLeft")
            .get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    db.getReference("users").child(userId).child("recommendationsLeft").setValue(10);
                }
            });
        
        db.getReference("users").child(userId).child("totalPoints")
            .get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    db.getReference("users").child(userId).child("totalPoints").setValue(0);
                }
            });
    }
    
    public static void resetDailyRecommendations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .child("recommendationsLeft").setValue(10);
    }
}
