package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.AmazonBedrockService.BedrockService;
import com.example.myapplication.WithingAPI.WithingsTokenManager;
import com.example.myapplication.analytics.AnalyticsTracker;
import com.example.myapplication.analytics.CloudWatchLogger;
import com.example.myapplication.WithingAPI.WithingsClient;
import com.example.myapplication.WithingAPI.HealthData;
import com.example.myapplication.utils.TTSHelperREST;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView chatBotText;
    private android.os.Handler handler;
    private int messageIndex = 0;
    private BedrockService bedrockService;
    private List<String> healthMessages = new ArrayList<>();
    private CloudWatchLogger cloudWatchLogger;

    private TextView textViewpollytest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AnalyticsTracker.init(requireContext());
        AnalyticsTracker.trackScreen("Dashboard");
        bedrockService = new BedrockService();
        cloudWatchLogger = new CloudWatchLogger(requireContext());
        chatBotText = view.findViewById(R.id.catamazonqchatter);
        setupHealthCardClicks();
        loadWithingsData();
        
        chatBotText.postDelayed(() -> analyzeHealthDataWithBedrock(), 500);

        textViewpollytest = view.findViewById(R.id.pollytest);


    }
    
    private void loadWithingsData() {
        String accessToken = WithingsTokenManager.getInstance(requireContext()).getAccessToken();
        if (accessToken == null) {
            android.util.Log.w("DashboardFragment", "No access token");
            return;
        }
        
        WithingsClient client = WithingsClient.getInstance();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        // Fetch blood pressure
        client.getBloodPressure(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView bpValue = getView().findViewById(R.id.bloodPressureValue);
                        if (data.getBloodPressure() != null && data.getBloodPressure().getSystolic() > 0) {
                            bpValue.setText(data.getBloodPressure().toString());
                            AnalyticsTracker.trackWithingsDataFetch("blood_pressure", true);
                            AnalyticsTracker.trackHealthMetric("systolic", data.getBloodPressure().getSystolic());
                        } else {
                            bpValue.setText("--/--");
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView bpValue = getView().findViewById(R.id.bloodPressureValue);
                        bpValue.setText("Error");
                    });
                }
            }
        });
        
        // Fetch steps
        client.getSteps(accessToken, today, today, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView stepsValue = getView().findViewById(R.id.stepsValue);
                        if (data.getStepsToday() > 0) {
                            stepsValue.setText(String.valueOf(data.getStepsToday()));
                        } else {
                            stepsValue.setText("0");
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView stepsValue = getView().findViewById(R.id.stepsValue);
                        stepsValue.setText("Error");
                    });
                }
            }
        });


        

        // Fetch weight
        client.getWeight(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView weightValue = getView().findViewById(R.id.weightValue);
                        if (data.getWeight() > 0) {
                            weightValue.setText(String.format("%.1f kg", data.getWeight()));
                        } else {
                            weightValue.setText("0.0 kg");
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        TextView weightValue = getView().findViewById(R.id.weightValue);
                        weightValue.setText("Error");
                    });
                }
            }
        });
    }
    
    private void analyzeHealthDataWithBedrock() {
        View view = getView();
        if (view == null) {
            android.util.Log.e("DashboardFragment", "❌ View is null");
            return;
        }
        
        chatBotText.setText("🤖 Analyzing your health data with AI...");
        android.util.Log.d("DashboardFragment", "🚀 Starting Bedrock analysis");
        
        view.postDelayed(() -> {
            TextView bloodPressureValue = view.findViewById(R.id.bloodPressureValue);
            TextView stepsValue = view.findViewById(R.id.stepsValue);
            TextView sleepValue = view.findViewById(R.id.sleepValue);
            TextView weightValue = view.findViewById(R.id.weightValue);
            
            String bp = bloodPressureValue.getText().toString();
            String steps = stepsValue.getText().toString();
            String sleep = sleepValue.getText().toString();
            String weight = weightValue.getText().toString();
            
            android.util.Log.d("DashboardFragment", "📊 Health Data Collected:");
            android.util.Log.d("DashboardFragment", "   BP: " + bp);
            android.util.Log.d("DashboardFragment", "   Steps: " + steps);
            android.util.Log.d("DashboardFragment", "   Sleep: " + sleep);
            android.util.Log.d("DashboardFragment", "   Weight: " + weight);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate 5 unique personalized health insights based on this real-time data:\n\n");
            prompt.append("Blood Pressure: ").append(bp).append("\n");
            prompt.append("Steps Today: ").append(steps).append("\n");
            prompt.append("Sleep Duration: ").append(sleep).append("\n");
            prompt.append("Weight: ").append(weight).append("\n\n");
            prompt.append("Create 5 different motivational messages. Each should:\n");
            prompt.append("- Reference the actual data values\n");
            prompt.append("- Be encouraging and actionable\n");
            prompt.append("- Include emojis (✅, 💪, ⭐, 🎯, ❤️)\n");
            prompt.append("- Vary vocabulary each time\n\n");
            prompt.append("Return ONLY 5 messages, one per line.");
            
            android.util.Log.d("DashboardFragment", "📤 Sending to Bedrock:");
            android.util.Log.d("DashboardFragment", prompt.toString());
            
            bedrockService.sendMessage(prompt.toString(), response -> {
                android.util.Log.d("DashboardFragment", "📥 Bedrock RAW response: " + response);
                android.util.Log.d("DashboardFragment", "📏 Response length: " + (response != null ? response.length() : 0));
                AnalyticsTracker.trackBedrockCall("health_insights", response != null ? response.length() : 0);
                cloudWatchLogger.logEvent("BEDROCK_HEALTH_ANALYSIS", "bp=" + bp + ",steps=" + steps + ",response_len=" + (response != null ? response.length() : 0));
                
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response == null || response.isEmpty()) {
                            android.util.Log.e("DashboardFragment", "❌ Empty response");
                            chatBotText.setText("⚠️ No response from AI");
                            return;
                        }
                        
                        if (response.startsWith("❌") || response.contains("API Error")) {
                            android.util.Log.e("DashboardFragment", "❌ API Error: " + response);
                            chatBotText.setText("⚠️ Unable to analyze health data");
                            return;
                        }
                        
                        healthMessages.clear();
                        String[] lines = response.split("\n");
                        android.util.Log.d("DashboardFragment", "📝 Parsing " + lines.length + " lines");
                        
                        for (int i = 0; i < lines.length; i++) {
                            String line = lines[i].trim();
                            
                            // Skip intro/header lines
                            if (line.toLowerCase().contains("here are") || 
                                line.toLowerCase().contains("tailored") ||
                                line.toLowerCase().contains("based on") ||
                                line.isEmpty()) {
                                android.util.Log.d("DashboardFragment", "   Skipping: " + line);
                                continue;
                            }
                            
                            // Remove numbering
                            line = line.replaceFirst("^\\d+\\.\\s*", "");
                            line = line.replaceFirst("^\\*\\*\\d+\\.\\*\\*\\s*", "");
                            
                            android.util.Log.d("DashboardFragment", "   Line " + i + ": [" + line + "] (len=" + line.length() + ")");
                            if (!line.isEmpty() && line.length() > 15) {
                                healthMessages.add(line);
                                android.util.Log.d("DashboardFragment", "   ✅ Added message " + healthMessages.size());
                            }
                        }
                        
                        android.util.Log.d("DashboardFragment", "✅ Total messages parsed: " + healthMessages.size());
                        
                        if (!healthMessages.isEmpty()) {
                            android.util.Log.d("DashboardFragment", "🔄 Starting message rotation");
                            cloudWatchLogger.logEvent("HEALTH_INSIGHTS_GENERATED", "count=" + healthMessages.size());
                            sendHealthInsightNotification(healthMessages.get(0));
                            startMessageRotation();
                            readHealthMessagesWithTTS();
                        } else {
                            android.util.Log.w("DashboardFragment", "⚠️ No messages to display");
                            chatBotText.setText("✅ Health data analyzed");
                        }
                    });
                } else {
                    android.util.Log.e("DashboardFragment", "❌ Activity is null");
                }
            });
        }, 1500);
    }
    
    private void parseAndDisplayHealthAnalysis(String jsonResponse) {
        try {
            android.util.Log.d("DashboardFragment", "Raw response: " + jsonResponse);
            
            // Remove markdown code blocks
            String cleanJson = jsonResponse.trim();
            if (cleanJson.contains("```")) {
                cleanJson = cleanJson.replaceAll("```[a-z-]*", "").replaceAll("```", "").trim();
            }
            
            android.util.Log.d("DashboardFragment", "Clean JSON: " + cleanJson);
            
            JSONArray results;
            if (cleanJson.startsWith("[")) {
                results = new JSONArray(cleanJson);
            } else {
                JSONObject json = new JSONObject(cleanJson);
                if (json.has("rows")) {
                    results = json.getJSONArray("rows");
                } else {
                    throw new Exception("Unknown JSON structure: " + cleanJson.substring(0, Math.min(100, cleanJson.length())));
                }
            }
            
            healthMessages.clear();
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                
                // Handle both capitalized and lowercase field names
                String message = result.optString("Message", result.optString("message", "Health metric recorded"));
                String status = result.optString("Status", result.optString("status", "normal")).toLowerCase();
                
                String emoji = status.equals("normal") ? "✅" : 
                              status.equals("warning") ? "⚠️" : "ℹ️";
                healthMessages.add(emoji + " " + message);
            }
            
            android.util.Log.d("DashboardFragment", "SUCCESS: Parsed " + healthMessages.size() + " messages");
            if (!healthMessages.isEmpty()) {
                startMessageRotation();
            } else {
                chatBotText.setText("✅ Health data analyzed");
            }
        } catch (Exception e) {
            android.util.Log.e("DashboardFragment", "Parse error: " + e.getMessage());
            e.printStackTrace();
            chatBotText.setText("✅ Health analysis complete. Keep up the good work!");
        }
    }

    private void startMessageRotation() {
        if (healthMessages.isEmpty()) return;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        handler = new android.os.Handler();
        messageIndex = 0;

        Runnable messageRotator = new Runnable() {
            @Override
            public void run() {
                // Sadece 5 mesaj okunacak
                if (chatBotText != null && messageIndex < healthMessages.size() && messageIndex < 5) {
                    String message = healthMessages.get(messageIndex);
                    chatBotText.setText(message);

                    // TTS ile oku
                    TTSHelperREST.speakText(getContext(), message, true, true);

                    messageIndex++;

                    // 10 saniye sonra bir sonraki mesaja geç
                    if (messageIndex < 5 && messageIndex < healthMessages.size()) {
                        handler.postDelayed(this, 10000); // 10 saniye bekle
                    }
                }
            }
        };

        handler.post(messageRotator);
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
    
    private void sendHealthInsightNotification(String healthInsight) {
        if (getActivity() == null) return;
        
        // Save to Firebase
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String notificationId = com.google.firebase.database.FirebaseDatabase.getInstance().getReference().push().getKey();
            java.util.Map<String, Object> notificationData = new java.util.HashMap<>();
            notificationData.put("title", "❤️ Health Insight");
            notificationData.put("message", healthInsight);
            notificationData.put("timestamp", System.currentTimeMillis());
            
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("notifications")
                .child(userId)
                .child(notificationId)
                .setValue(notificationData);
        }
        
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) 
            getActivity().getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelId = "health_insights";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                channelId,
                "Health Insights",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            notificationManager.createNotificationChannel(channel);
        }
        
        Intent intent = new Intent(getActivity(), ReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
            getActivity(), 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        );
        
        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(getActivity(), channelId)
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle("❤️ Health Insight")
            .setContentText(healthInsight)
            .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(healthInsight))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(pendingIntent);
        
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        android.util.Log.d("DashboardFragment", "✅ Health insight notification sent");
    }

    private void readHealthMessagesWithTTS() {
        if (healthMessages.isEmpty()) return;

        android.os.Handler ttsHandler = new android.os.Handler();
        final int[] ttsIndex = {0};

        Runnable ttsRunner = new Runnable() {
            @Override
            public void run() {
                if (!healthMessages.isEmpty() && ttsIndex[0] < healthMessages.size()) {
                    String message = healthMessages.get(ttsIndex[0]);
                    // Burada kendi kedi-çocuk TTS fonksiyonunu çağır


                    ttsIndex[0]++;
                    ttsHandler.postDelayed(this, 5000); // 5 saniye arayla
                }
            }
        };

        ttsHandler.post(ttsRunner);
    }


    
    private void setupHealthCardClicks() {
        View.OnClickListener openGetClientActivity = v -> {
            Intent intent = new Intent(getActivity(), GetClientActivity.class);
            startActivity(intent);
        };
    }
}