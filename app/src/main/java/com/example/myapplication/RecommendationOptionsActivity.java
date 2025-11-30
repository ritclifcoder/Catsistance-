package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.HashMap;
import java.util.Map;

public class RecommendationOptionsActivity extends AppCompatActivity {

    private String userId;
    private String targetUserId;
    private String targetUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_options);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        targetUserId = getIntent().getStringExtra("targetUserId");
        targetUserName = getIntent().getStringExtra("targetUserName");
        
        TextView titleText = findViewById(R.id.titleText);
        if (targetUserName != null) {
            titleText.setText("Send Health Recommendation to " + targetUserName);
        }

        // Force reset to 10 first
        FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/")
            .getReference("Users").child(userId).child("recommendationsLeft").setValue(10)
            .addOnSuccessListener(aVoid -> {
                findViewById(R.id.btnDrinkWater).setOnClickListener(v -> sendRecommendation("Drink more water"));
                findViewById(R.id.btnWalkMore).setOnClickListener(v -> sendRecommendation("Walk more"));
                findViewById(R.id.btnLowerBP).setOnClickListener(v -> sendRecommendation("Lower your blood pressure"));
                findViewById(R.id.btnReduceStress).setOnClickListener(v -> sendRecommendation("Reduce stress levels"));
            });
    }

    private void sendRecommendation(String message) {
        if (targetUserId == null) {
            Toast.makeText(this, "Invalid target user", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        db.getReference("Users").child(userId).child("recommendationsLeft")
            .get().addOnSuccessListener(snapshot -> {
                int left = snapshot.exists() ? snapshot.getValue(Integer.class) : 10;
                
                if (left <= 0) {
                    Toast.makeText(this, "No recommendations left!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String recId = db.getReference("recommendations").push().getKey();
                Map<String, Object> rec = new HashMap<>();
                rec.put("from", userId);
                rec.put("to", targetUserId);
                rec.put("message", message);
                rec.put("timestamp", System.currentTimeMillis());
                db.getReference("recommendations").child(recId).setValue(rec);
                
                // Write to sender's recommendations node
                db.getReference("Users").child(userId).child("recommendations").child(recId).setValue(rec);
                
                // Write to receiver's receivedRecommendations node
                db.getReference("Users").child(targetUserId).child("receivedRecommendations").child(recId).setValue(rec);

                int newLeft = left - 1;
                db.getReference("Users").child(userId).child("recommendationsLeft").setValue(newLeft);
                
                db.getReference("Users").child(userId).child("points")
                    .get().addOnSuccessListener(pointsSnapshot -> {
                        int currentPoints = pointsSnapshot.exists() ? pointsSnapshot.getValue(Integer.class) : 0;
                        int earnedXP = 10;
                        int newPoints = currentPoints + earnedXP;
                        db.getReference("Users").child(userId).child("points").setValue(newPoints);
                        db.getReference("Users").child("points").child(userId).setValue(newPoints);

                        Intent intent = new Intent(RecommendationOptionsActivity.this, CongratulationsActivity.class);
                        intent.putExtra("xpEarned", earnedXP);
                        intent.putExtra("recommendationsLeft", newLeft);
                        startActivity(intent);
                        finish();
                    });
            });
    }
}
