package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UsersActivity extends AppCompatActivity {
    
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_POINTS = "user_points";
    public static final String EXTRA_USER_STATS = "user_stats";
    public static final String EXTRA_USER_VITALS = "user_vitals";
    public static final String EXTRA_USER_PHOTO_URI = "user_photo_uri";
    
    private String targetUserId;
    private String targetUserName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        
        targetUserName = getIntent().getStringExtra(EXTRA_USER_NAME);
        targetUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        int userPoints = getIntent().getIntExtra(EXTRA_USER_POINTS, 0);
        String userStats = getIntent().getStringExtra(EXTRA_USER_STATS);
        String userVitals = getIntent().getStringExtra(EXTRA_USER_VITALS);
        String photoUri = getIntent().getStringExtra(EXTRA_USER_PHOTO_URI);
        
        populatePlayerCard(targetUserName, userPoints, userStats, userVitals);
        loadProfilePhoto(photoUri);
        loadUserGroup(targetUserId);
        
        Button recommendBtn = findViewById(R.id.recommendActivityButton);
        recommendBtn.setOnClickListener(v -> {
            if (targetUserId == null) {
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
            
            // Force reset to 10
            db.getReference("Users").child(currentUserId).child("recommendationsLeft").setValue(10)
                .addOnSuccessListener(aVoid -> {
                    proceedWithRecommendation(currentUserId, db);
                });
        });
    }
    
    private void proceedWithRecommendation(String currentUserId, FirebaseDatabase db) {
        db.getReference("Users").child(currentUserId).get().addOnSuccessListener(currentUserSnapshot -> {
                String currentUserGroup = currentUserSnapshot.child("group").getValue(String.class);
                int left = currentUserSnapshot.child("recommendationsLeft").exists() ? 
                    currentUserSnapshot.child("recommendationsLeft").getValue(Integer.class) : 10;
                
                if (left <= 0) {
                    Toast.makeText(this, "No recommendations left today!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                db.getReference("Users").child(targetUserId).child("group").get()
                    .addOnSuccessListener(targetGroupSnapshot -> {
                        String targetUserGroup = targetGroupSnapshot.getValue(String.class);
                        
                        android.util.Log.d("UsersActivity", "Current group: '" + currentUserGroup + "' Target group: '" + targetUserGroup + "'");
                        
                        if (currentUserGroup != null && targetUserGroup != null && 
                            currentUserGroup.trim().equalsIgnoreCase(targetUserGroup.trim())) {
                            Intent intent = new Intent(UsersActivity.this, RecommendationOptionsActivity.class);
                            intent.putExtra("targetUserId", targetUserId);
                            intent.putExtra("targetUserName", targetUserName);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "You can only send recommendations to your group members! (" + currentUserGroup + " vs " + targetUserGroup + ")", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
    
    private void populatePlayerCard(String name, int points, String stats, String vitals) {
        ((TextView)findViewById(R.id.playerName)).setText(name.toUpperCase());
        ((TextView)findViewById(R.id.playerRating)).setText(String.valueOf(points / 10));
        ((TextView)findViewById(R.id.playerPosition)).setText("HW");
        
        String[] statsParts = stats.split("\\|");
        String[] vitalsParts = vitals.split("\\|");
        
        int steps = extractNumber(statsParts[0]);
        double sleep = extractDecimal(statsParts[1]);
        int systolic = extractNumber(vitalsParts[0].split("/")[0]);
        int heartRate = extractNumber(vitalsParts[1]);
        
        int stp = Math.min(99, steps / 130);
        int slp = Math.min(99, (int)(sleep * 12));
        int bp = Math.max(50, 140 - systolic);
        int hr = Math.max(50, 120 - heartRate);
        int cal = points / 15;
        int wgt = 85 + (points % 15);
        
        ((TextView)findViewById(R.id.cardioPower)).setText(slp + " SLP");
        ((TextView)findViewById(R.id.endurance)).setText(bp + " BP");
        ((TextView)findViewById(R.id.vitality)).setText(hr + " HR");
        ((TextView)findViewById(R.id.discipline)).setText(cal + " CAL");
        ((TextView)findViewById(R.id.recovery)).setText(wgt + " WGT");
    }
    
    private int extractNumber(String text) {
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }
    
    private double extractDecimal(String text) {
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }
    
    private void loadProfilePhoto(String photoUri) {
        android.widget.ImageView profilePhoto = findViewById(R.id.playerAvatar);
        if (profilePhoto == null) return;
        
        if (photoUri != null && !photoUri.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(photoUri);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            profilePhoto.setImageBitmap(getCircularBitmap(bitmap));
                        } else {
                            profilePhoto.setImageResource(R.drawable.catpuccino);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> profilePhoto.setImageResource(R.drawable.catpuccino));
                }
            }).start();
        } else {
            profilePhoto.setImageResource(R.drawable.catpuccino);
        }
    }
    
    private android.graphics.Bitmap getCircularBitmap(android.graphics.Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        android.graphics.Bitmap output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f, paint);
        return output;
    }
    
    private void loadUserGroup(String userId) {
        if (userId == null) return;
        
        FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/")
            .getReference("Users")
            .child(userId)
            .child("group")
            .get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String group = snapshot.getValue(String.class);
                    TextView playerPosition = findViewById(R.id.playerPosition);
                    if (playerPosition != null && group != null) {
                        playerPosition.setText(group.toUpperCase());
                    }
                }
            });
    }
}