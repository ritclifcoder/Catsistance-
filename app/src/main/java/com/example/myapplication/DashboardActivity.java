package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.example.myapplication.AmazonBedrockService.BedrockService;
import com.example.myapplication.WithingAPI.WithingsTokenManager;
import com.example.myapplication.WithingAPI.WithingsClient;
import com.example.myapplication.WithingAPI.HealthData;
import com.example.myapplication.healthconnect.HealthConnectManager;
import androidx.health.connect.client.PermissionController;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;


public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView chatBotText;
    private android.os.Handler handler;
    private int messageIndex = 0;
    private java.util.List<String> messages;
    private BedrockService bedrockService;
    private List<String> healthMessages = new ArrayList<>();
    private static final int REQUEST_WITHINGS_AUTH = 1001;
    private android.view.MenuItem notificationMenuItem;
    private boolean hasUnreadNotifications = false;



    private TextView textViewpollytest;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        android.util.Log.d("PollyTest", "onCreate içinde testPolly çağrılıyor");




       

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_signout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_health_tracking) {
                drawerLayout.closeDrawers();
                android.widget.Toast.makeText(DashboardActivity.this, "🔒 Authenticating with Withings...", android.widget.Toast.LENGTH_SHORT).show();
                startWithingsAuth();
                return true;
            } else if (item.getItemId() == R.id.nav_reminders) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(DashboardActivity.this, ReminderActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_recommendation_history) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(DashboardActivity.this, RecommendationHistoryActivity.class));
                return true;
            }
            return false;
        });

        bedrockService = new BedrockService();
        chatBotText = findViewById(R.id.catamazonqchatter);
        setupHealthCardClicks();

        // Check if authenticated with Withings
        if (WithingsTokenManager.getInstance(this).isAuthenticated()) {
            loadWithingsData();
        } else {
            android.widget.Toast.makeText(this, "⚠️ Please authenticate with Withings to see real data", android.widget.Toast.LENGTH_LONG).show();
        }

        // Delay to ensure layout is ready
        chatBotText.postDelayed(() -> analyzeHealthDataWithBedrock(), 500);
    }

    private void analyzeHealthDataWithBedrock() {
        if (chatBotText == null) {
            chatBotText = findViewById(R.id.catamazonqchatter);
        }

        // Get current health values
        TextView bloodPressureValue = findViewById(R.id.bloodPressureValue);
        TextView stepsValue = findViewById(R.id.stepsValue);
        TextView sleepValue = findViewById(R.id.sleepValue);
        TextView waterIntakeValue = findViewById(R.id.waterIntakeValue);

        List<String> healthInputs = new ArrayList<>();
        healthInputs.add("Blood Pressure: " + bloodPressureValue.getText().toString());
        healthInputs.add("Steps Today: " + stepsValue.getText().toString());
        healthInputs.add("Sleep Duration: " + sleepValue.getText().toString());
        healthInputs.add("Water Intake: " + waterIntakeValue.getText().toString());

        android.util.Log.d("DashboardActivity", "Starting health analysis");
        chatBotText.setText("🤖 Analyzing your health data with AI...");
        
        // TEST: Add manual messages to verify rotation works
        healthMessages.clear();
        healthMessages.add("✅ Blood Pressure: 120/80 - Normal range");
        healthMessages.add("✅ Steps: 8,542 - Great activity level!");
        healthMessages.add("✅ Sleep: 7h 30m - Good rest");
        healthMessages.add("✅ Water: 1.8L - Well hydrated");

        android.util.Log.d("DashboardActivity", "Messages added: " + healthMessages.size());

        chatBotText.postDelayed(() -> {
            android.util.Log.d("DashboardActivity", "Starting rotation with " + healthMessages.size() + " messages");
            if (healthMessages.size() > 0) {
                startMessageRotation();
            } else {
                android.util.Log.e("DashboardActivity", "No messages to rotate!");
            }
        }, 2000);

        android.util.Log.d("DashboardActivity", "Calling Bedrock API with " + healthInputs.size() + " inputs");

        bedrockService.analyzeHealthData(healthInputs, response -> {
            android.util.Log.d("DashboardActivity", "Bedrock callback received");
            runOnUiThread(() -> {
                android.util.Log.d("DashboardActivity", "Bedrock Response received: " + response.substring(0, Math.min(200, response.length())));
                try {
                    parseAndDisplayHealthAnalysis(response);
                } catch (Exception e) {
                    android.util.Log.e("DashboardActivity", "Parse error: " + e.getMessage());
                    e.printStackTrace();
                    // Show the raw response if JSON parsing fails
                    chatBotText.setText(response);

                    // Try to rotate through health inputs as fallback
                    healthMessages.clear();
                    for (String input : healthInputs) {
                        healthMessages.add("✅ " + input + " - Looking good!");
                    }
                    if (!healthMessages.isEmpty()) {
                        startMessageRotation();
                    }
                }
            });
        });
    }

    private void parseAndDisplayHealthAnalysis(String jsonResponse) {
        try {
            // Parse Bedrock response format
            JSONObject bedrockResponse = new JSONObject(jsonResponse);
            JSONArray bedrockResults = bedrockResponse.getJSONArray("results");
            String outputText = bedrockResults.getJSONObject(0).getString("outputText").trim();

            android.util.Log.d("DashboardActivity", "OutputText: " + outputText);

            // Try to parse outputText as JSON
            JSONObject json = new JSONObject(outputText);
            JSONArray results = json.getJSONArray("results");

            healthMessages.clear();
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String message = result.getString("message");
                String status = result.getString("status");
                int score = result.getInt("score");

                String emoji = status.equals("normal") ? "✅" :
                              status.equals("warning") ? "⚠️" : "ℹ️";
                healthMessages.add(emoji + " " + message + " (Score: " + score + ")");
            }

            startMessageRotation();
        } catch (Exception e) {
            android.util.Log.e("DashboardActivity", "Parse failed: " + e.getMessage());
            chatBotText.setText("✅ Health analysis complete. Keep up the good work!");
        }

    }

    private void startMessageRotation() {
        android.util.Log.d("DashboardActivity", "startMessageRotation called, messages: " + healthMessages.size());

        if (healthMessages.isEmpty()) {
            android.util.Log.e("DashboardActivity", "No messages to rotate!");
            return;
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        handler = new android.os.Handler();
        messageIndex = 0;

        Runnable messageRotator = new Runnable() {
            @Override
            public void run() {
                if (chatBotText != null && !healthMessages.isEmpty()) {
                    String msg = healthMessages.get(messageIndex);
                    android.util.Log.d("DashboardActivity", "Showing message " + messageIndex + ": " + msg);
                    chatBotText.setText(msg);
                    messageIndex = (messageIndex + 1) % healthMessages.size();
                    handler.postDelayed(this, 5000);
                }
            }
        };
        handler.post(messageRotator);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        notificationMenuItem = menu.findItem(R.id.action_notifications);
        checkForUnreadNotifications();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chat) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_notifications) {
            hasUnreadNotifications = false;
            updateNotificationIcon();
            Intent intent = new Intent(this, ReminderActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile || id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void setupHealthCardClicks() {
        View.OnClickListener openGetClientActivity = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, GetClientActivity.class);
                startActivity(intent);
            }
        };
    }

    private void startWithingsAuth() {
        android.util.Log.d("DashboardActivity", "startWithingsAuth() called");
        try {
            Intent intent = new Intent(this, com.example.myapplication.withings.WithingsAuthActivity.class);
            android.util.Log.d("DashboardActivity", "Starting WithingsAuthActivity...");
            startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("DashboardActivity", "Error starting auth: " + e.getMessage());
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void requestHealthConnectPermissions() {
        HealthConnectManager manager = HealthConnectManager.Companion.getInstance(this);
        if (!manager.isAvailable()) {
            android.widget.Toast.makeText(this, "⚠️ Health Connect not available. Using demo data.", android.widget.Toast.LENGTH_LONG).show();
            loadMockData();
            return;
        }

        Intent intent = PermissionController.createRequestPermissionResultContract()
            .createIntent(this, HealthConnectManager.Companion.getPERMISSIONS());
        startActivityForResult(intent, 2001);
    }

    private void loadHealthConnectData() {
        HealthConnectManager manager = HealthConnectManager.Companion.getInstance(this);

        manager.getTodaySteps(new HealthConnectManager.HealthDataCallback() {
            @Override
            public void onStepsReceived(int steps) {
                runOnUiThread(() -> {
                    TextView stepsValue = findViewById(R.id.stepsValue);
                    if (steps > 0) {
                        stepsValue.setText(String.valueOf(steps));
                        android.widget.Toast.makeText(DashboardActivity.this, "✅ Steps: " + steps, android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        stepsValue.setText("8542");
                    }
                });
            }

            @Override
            public void onBloodPressureReceived(int systolic, int diastolic) {}

            @Override
            public void onSleepReceived(int hours, int minutes) {}

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    TextView stepsValue = findViewById(R.id.stepsValue);
                    stepsValue.setText("8542");
                });
            }
        });

        manager.getTodayBloodPressure(new HealthConnectManager.HealthDataCallback() {
            @Override
            public void onStepsReceived(int steps) {}

            @Override
            public void onBloodPressureReceived(int systolic, int diastolic) {
                runOnUiThread(() -> {
                    TextView bpValue = findViewById(R.id.bloodPressureValue);
                    bpValue.setText(systolic + "/" + diastolic);
                });
            }

            @Override
            public void onSleepReceived(int hours, int minutes) {}

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    TextView bpValue = findViewById(R.id.bloodPressureValue);
                    bpValue.setText("120/80");
                });
            }
        });

        manager.getTodaySleep(new HealthConnectManager.HealthDataCallback() {
            @Override
            public void onStepsReceived(int steps) {}

            @Override
            public void onBloodPressureReceived(int systolic, int diastolic) {}

            @Override
            public void onSleepReceived(int hours, int minutes) {
                runOnUiThread(() -> {
                    TextView sleepValue = findViewById(R.id.sleepValue);
                    sleepValue.setText(hours + "h " + minutes + "m");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    TextView sleepValue = findViewById(R.id.sleepValue);
                    sleepValue.setText("7h 30m");
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WITHINGS_AUTH && resultCode == RESULT_OK) {
            android.widget.Toast.makeText(this, "Connected to Withings!", android.widget.Toast.LENGTH_SHORT).show();
            loadWithingsData();
        } else if (requestCode == 2001) {
            android.widget.Toast.makeText(this, "📊 Loading health data...", android.widget.Toast.LENGTH_SHORT).show();
            loadHealthConnectData();
        }
    }

    private void loadWithingsData() {
        String accessToken = WithingsTokenManager.getInstance(this).getAccessToken();
        if (accessToken == null) {
            android.util.Log.w("DashboardActivity", "No access token");
            android.widget.Toast.makeText(this, "⚠️ Please authenticate with Withings first", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        android.util.Log.d("DashboardActivity", "Starting Withings API calls...");
        android.widget.Toast.makeText(this, "Fetching health data...", android.widget.Toast.LENGTH_SHORT).show();

        WithingsClient client = WithingsClient.getInstance();

        // Fetch blood pressure
        android.util.Log.d("DashboardActivity", "Calling getBloodPressure API...");
        client.getBloodPressure(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                android.util.Log.d("DashboardActivity", "✅ BP CALLBACK TRIGGERED");
                android.util.Log.d("DashboardActivity", "✅ Blood Pressure API SUCCESS");
                android.util.Log.d("DashboardActivity", "BP Data: " + (data.getBloodPressure() != null ? data.getBloodPressure().toString() : "null"));

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (userId != null) {
                    FirebaseDatabase db = FirebaseDatabase.getInstance("https://cat-sistance-default-rtdb.europe-west1.firebasedatabase.app/");
                    if (data.getBloodPressure() != null && data.getBloodPressure().getSystolic() > 0) {
                        db.getReference("Users").child(userId).child("vitals").child("systolic").setValue(data.getBloodPressure().getSystolic());
                        db.getReference("Users").child(userId).child("vitals").child("diastolic").setValue(data.getBloodPressure().getDiastolic());
                        db.getReference("Users").child(userId).child("vitals").child("heartRate").setValue(72);
                        android.util.Log.d("DashboardActivity", "✅ [" + userId + "] BP written to Firebase: " + data.getBloodPressure().toString());
                    } else {
                        android.util.Log.e("DashboardActivity", "❌ BP is null or 0, writing defaults");
                        db.getReference("Users").child(userId).child("vitals").child("systolic").setValue(100);
                        db.getReference("Users").child(userId).child("vitals").child("diastolic").setValue(100);
                        db.getReference("Users").child(userId).child("vitals").child("heartRate").setValue(72);
                    }
                }

                runOnUiThread(() -> {
                    TextView bpValue = findViewById(R.id.bloodPressureValue);
                    if (data.getBloodPressure() != null && data.getBloodPressure().getSystolic() > 0) {
                        String value = data.getBloodPressure().toString();
                        bpValue.setText(value);
                        android.widget.Toast.makeText(DashboardActivity.this, "✅ BP: " + value, android.widget.Toast.LENGTH_SHORT).show();
                        sendHealthUpdateNotification("Blood Pressure Updated", "Your BP is now " + value);
                    } else {
                        bpValue.setText("100/100");
                        android.util.Log.d("DashboardActivity", "BP empty from API, showing default");
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("DashboardActivity", "❌ BP API ERROR: " + error);
                runOnUiThread(() -> {
                    TextView bpValue = findViewById(R.id.bloodPressureValue);
                    bpValue.setText("Error");
                });
            }
        });

        // Fetch steps (today)
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        android.util.Log.d("DashboardActivity", "Calling getSteps API for date: " + today);
        client.getSteps(accessToken, today, today, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                android.util.Log.d("DashboardActivity", "✅ Steps API SUCCESS");
                runOnUiThread(() -> {
                    TextView stepsValue = findViewById(R.id.stepsValue);
                    if (data.getStepsToday() > 0) {
                        String value = String.valueOf(data.getStepsToday());
                        stepsValue.setText(value);
                        android.util.Log.d("DashboardActivity", "Steps from API: " + value);
                        android.widget.Toast.makeText(DashboardActivity.this, "✅ Steps: " + value, android.widget.Toast.LENGTH_SHORT).show();

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (userId != null) {
                            FirebaseDatabase.getInstance("https://cat-sistance-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference("Users").child(userId).child("healthStats").child("steps").setValue(data.getStepsToday())
                                .addOnSuccessListener(aVoid -> android.util.Log.d("DashboardActivity", "✅ [" + userId + "] Steps saved to Firebase"))
                                .addOnFailureListener(e -> android.util.Log.e("DashboardActivity", "❌ Steps save failed: " + e.getMessage()));
                        }

                        sendHealthUpdateNotification("Steps Updated", "You've walked " + value + " steps today! 🚶");
                    } else {
                        stepsValue.setText("0");
                        android.util.Log.d("DashboardActivity", "Steps empty from API");
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("DashboardActivity", "❌ Steps API ERROR: " + error);
                runOnUiThread(() -> {
                    TextView stepsValue = findViewById(R.id.stepsValue);
                    stepsValue.setText("Error");
                });
            }
        });


        // Fetch weight
        android.util.Log.d("DashboardActivity", "Calling getWeight API...");
        client.getWeight(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                android.util.Log.d("DashboardActivity", "✅ Weight API SUCCESS");
                runOnUiThread(() -> {
                    TextView weightValue = findViewById(R.id.weightValue);
                    if (data.getWeight() > 0) {
                        String value = String.format("%.1f kg", data.getWeight());
                        weightValue.setText(value);
                        android.util.Log.d("DashboardActivity", "Weight from API: " + value);
                        android.widget.Toast.makeText(DashboardActivity.this, "✅ Weight: " + value, android.widget.Toast.LENGTH_SHORT).show();
                        sendHealthUpdateNotification("Weight Updated", "Current weight: " + value + " ⚖️");
                    } else {
                        weightValue.setText("0.0 kg");
                        android.util.Log.d("DashboardActivity", "Weight empty from API");
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("DashboardActivity", "❌ Weight API ERROR: " + error);
                runOnUiThread(() -> {
                    TextView weightValue = findViewById(R.id.weightValue);
                    weightValue.setText("Error");
                });
            }
        });

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId != null) {
            FirebaseDatabase.getInstance("https://cat-sistance-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child("healthStats").child("sleepHours").setValue(7.5)
                .addOnSuccessListener(aVoid -> android.util.Log.d("DashboardActivity", "✅ [" + userId + "] Sleep saved to Firebase"))
                .addOnFailureListener(e -> android.util.Log.e("DashboardActivity", "❌ Sleep save failed: " + e.getMessage()));
        }
    }



    private void showManualDataEntryDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        android.widget.EditText stepsInput = new android.widget.EditText(this);
        stepsInput.setHint("Steps (e.g., 8000)");
        stepsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(stepsInput);

        android.widget.EditText bpSystolicInput = new android.widget.EditText(this);
        bpSystolicInput.setHint("Systolic BP (e.g., 120)");
        bpSystolicInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(bpSystolicInput);

        android.widget.EditText bpDiastolicInput = new android.widget.EditText(this);
        bpDiastolicInput.setHint("Diastolic BP (e.g., 80)");
        bpDiastolicInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(bpDiastolicInput);

        builder.setTitle("📝 Manual Data Entry")
            .setView(layout)
            .setPositiveButton("Submit to Withings", (dialog, which) -> {
                String steps = stepsInput.getText().toString();
                String systolic = bpSystolicInput.getText().toString();
                String diastolic = bpDiastolicInput.getText().toString();

                if (!steps.isEmpty() && !systolic.isEmpty() && !diastolic.isEmpty()) {
                    sendDataToWithings(Integer.parseInt(steps), Integer.parseInt(systolic), Integer.parseInt(diastolic));
                } else {
                    android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendDataToWithings(int steps, int systolic, int diastolic) {
        android.util.Log.d("DashboardActivity", "sendDataToWithings called: steps=" + steps + ", BP=" + systolic + "/" + diastolic);

        String token = getSharedPreferences("withings", MODE_PRIVATE).getString("access_token", null);
        android.util.Log.d("DashboardActivity", "Token: " + (token != null ? "exists" : "null"));

        if (token == null) {
            android.widget.Toast.makeText(this, "❌ Please authenticate with Withings first", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        android.widget.Toast.makeText(this, "📤 Sending to Withings API...", android.widget.Toast.LENGTH_SHORT).show();

        com.example.myapplication.withings.WithingsApiService apiService = new com.example.myapplication.withings.WithingsApiService();
        apiService.sendHealthData(token, steps, systolic, diastolic, (success, response) -> {
            runOnUiThread(() -> {
                android.util.Log.d("DashboardActivity", "API Response - Success: " + success + ", Response: " + response);

                String message = success ?
                    "✅ API Response: " + response :
                    "❌ Failed: " + response;

                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();

                if (success) {
                    TextView stepsValue = findViewById(R.id.stepsValue);
                    TextView bpValue = findViewById(R.id.bloodPressureValue);
                    stepsValue.setText(String.valueOf(steps));
                    bpValue.setText(systolic + "/" + diastolic);

                    // Send notifications for manual data entry
                    sendHealthUpdateNotification("Health Data Updated", "BP: " + systolic + "/" + diastolic + ", Steps: " + steps);
                }
            });
            return kotlin.Unit.INSTANCE;
        });
    }

    private void checkForUnreadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("notifications")
            .child(userId)
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        hasUnreadNotifications = true;
                        updateNotificationIcon();
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
            });
    }

    private void updateNotificationIcon() {
        if (notificationMenuItem != null) {
            if (hasUnreadNotifications) {
                notificationMenuItem.getIcon().setTint(getResources().getColor(R.color.primary_green));
            } else {
                notificationMenuItem.getIcon().setTintList(null);
            }
        }
    }

    private void loadMockData() {
        TextView bpValue = findViewById(R.id.bloodPressureValue);
        TextView stepsValue = findViewById(R.id.stepsValue);
        TextView sleepValue = findViewById(R.id.sleepValue);
        TextView weightValue = findViewById(R.id.weightValue);

        bpValue.setText("120/80");
        stepsValue.setText("8542");
        sleepValue.setText("7h 30m");
        weightValue.setText("70.0 kg");
    }
















    private void sendHealthUpdateNotification(String title, String message) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    com.example.myapplication.Notification.SendNotification notification =
                        new com.example.myapplication.Notification.SendNotification(
                            token,
                            "📊 " + title,
                            message,
                            this
                        );
                    notification.SendNotifications();
                    android.util.Log.d("DashboardActivity", "Sent notification: " + title);
                }
            });
    }




}