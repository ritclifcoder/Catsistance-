package com.example.myapplication.AmazonBedrockService;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.*;
import com.example.myapplication.analytics.CloudWatchLogger;

public class BedrockWarningEngine {
    
    private static BedrockService bedrockService = new BedrockService();
    private static List<String> cachedMessages = new ArrayList<>();
    private static boolean isGenerating = false;
    private static CloudWatchLogger cloudWatchLogger;
    
    public static void reset() {
        isGenerating = false;
        cachedMessages.clear();
        android.util.Log.d("BedrockWarning", "🔄 Engine reset");
    }
    
    static class GroupStats {
        String groupName;
        int userCount = 0;
        int totalSteps = 0;
        double totalSleep = 0;
        int totalSystolic = 0;
        int totalHeartRate = 0;
        int totalPoints = 0;
        
        GroupStats(String name) {
            this.groupName = name;
        }
    }
    
    public interface MessageCallback {
        void onMessagesGenerated(List<String> messages);
    }
    
    public static void generateWarningMessages(MessageCallback callback) {
        if (isGenerating) {
            android.util.Log.w("BedrockWarning", "⚠️ Already generating, returning cached messages");
            callback.onMessagesGenerated(cachedMessages.isEmpty() ? getFallbackMessages() : cachedMessages);
            return;
        }
        
        android.util.Log.d("BedrockWarning", "🚀 Starting new generation");
        if (cloudWatchLogger != null) {
            cloudWatchLogger.logEvent("WARNING_GENERATION_STARTED", "status=initiated");
        }
        isGenerating = true;
        fetchGroupDataAndAnalyze(callback);
    }
    
    private static void fetchGroupDataAndAnalyze(MessageCallback callback) {
        android.util.Log.d("BedrockWarning", "🚀 Fetching real data from Firebase");
        
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    android.util.Log.d("BedrockWarning", "📥 Firebase data received, users: " + snapshot.getChildrenCount());
                    if (cloudWatchLogger != null) {
                        cloudWatchLogger.logEvent("GROUP_DATA_FETCHED", "users=" + snapshot.getChildrenCount());
                    }
                    
                    Map<String, GroupStats> groupStats = new HashMap<>();
                    groupStats.put("Silver", new GroupStats("Silver"));
                    groupStats.put("Gold", new GroupStats("Gold"));
                    groupStats.put("Master", new GroupStats("Master"));
                    groupStats.put("Elite", new GroupStats("Elite"));
                    
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String group = userSnapshot.child("group").getValue(String.class);
                        
                        if (group != null && groupStats.containsKey(group)) {
                            GroupStats stats = groupStats.get(group);
                            stats.userCount++;
                            
                            Integer steps = userSnapshot.child("healthStats").child("steps").getValue(Integer.class);
                            if (steps != null) stats.totalSteps += steps;
                            
                            Double sleep = userSnapshot.child("healthStats").child("sleep").getValue(Double.class);
                            if (sleep != null) stats.totalSleep += sleep;
                            
                            Integer systolic = userSnapshot.child("vitals").child("systolic").getValue(Integer.class);
                            if (systolic != null) stats.totalSystolic += systolic;
                            
                            Integer heartRate = userSnapshot.child("vitals").child("heartRate").getValue(Integer.class);
                            if (heartRate != null) stats.totalHeartRate += heartRate;
                            
                            Integer points = userSnapshot.child("points").getValue(Integer.class);
                            if (points != null) stats.totalPoints += points;
                            
                            android.util.Log.d("BedrockWarning", "👤 User " + userId + " [" + group + "] - Steps: " + steps + ", Sleep: " + sleep + ", BP: " + systolic + ", HR: " + heartRate + ", Points: " + points);
                        }
                    }
                    
                    android.util.Log.d("BedrockWarning", "\n📊 ===== GROUP STATISTICS =====");
                    for (Map.Entry<String, GroupStats> entry : groupStats.entrySet()) {
                        GroupStats stats = entry.getValue();
                        if (stats.userCount > 0) {
                            android.util.Log.d("BedrockWarning", "\n🏆 " + entry.getKey() + " Group:");
                            android.util.Log.d("BedrockWarning", "   Users: " + stats.userCount);
                            android.util.Log.d("BedrockWarning", "   Avg Steps: " + (stats.totalSteps / stats.userCount));
                            android.util.Log.d("BedrockWarning", "   Avg Sleep: " + String.format("%.1f", stats.totalSleep / stats.userCount) + "h");
                            android.util.Log.d("BedrockWarning", "   Avg BP: " + (stats.totalSystolic / stats.userCount));
                            android.util.Log.d("BedrockWarning", "   Avg HR: " + (stats.totalHeartRate / stats.userCount) + " bpm");
                            android.util.Log.d("BedrockWarning", "   Avg Points: " + (stats.totalPoints / stats.userCount));
                        } else {
                            android.util.Log.d("BedrockWarning", "⚠️ " + entry.getKey() + " Group: NO USERS");
                        }
                    }
                    android.util.Log.d("BedrockWarning", "\n✅ Data processed, sending to Bedrock");
                    if (cloudWatchLogger != null) {
                        cloudWatchLogger.logEvent("GROUP_ANALYSIS_COMPLETE", "groups=4,totalUsers=" + 
                            (groupStats.get("Silver").userCount + groupStats.get("Gold").userCount + 
                             groupStats.get("Master").userCount + groupStats.get("Elite").userCount));
                    }
                    analyzeGroupsWithBedrock(groupStats, callback);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("BedrockWarning", "❌ Firebase error: " + error.getMessage());
                    isGenerating = false;
                    callback.onMessagesGenerated(getFallbackMessages());
                }
            });
    }
    
    private static void analyzeGroupsWithBedrock(Map<String, GroupStats> groupStats, MessageCallback callback) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[Request ID: ").append(System.currentTimeMillis()).append("]\n");
        prompt.append("IMPORTANT: Generate completely unique messages. Never repeat previous responses. Use creative and varied language.\n\n");
        prompt.append("Analyze these health group statistics and generate 10 urgent warning messages:\n\n");
        
        for (Map.Entry<String, GroupStats> entry : groupStats.entrySet()) {
            GroupStats stats = entry.getValue();
            if (stats.userCount > 0) {
                prompt.append(entry.getKey()).append(" Group:\n");
                prompt.append("- Users: ").append(stats.userCount).append("\n");
                prompt.append("- Avg Steps: ").append(stats.totalSteps / stats.userCount).append("\n");
                prompt.append("- Avg Sleep: ").append(String.format("%.1f", stats.totalSleep / stats.userCount)).append("h\n");
                prompt.append("- Avg BP: ").append(stats.totalSystolic / stats.userCount).append("\n");
                prompt.append("- Avg HR: ").append(stats.totalHeartRate / stats.userCount).append(" bpm\n");
                prompt.append("- Avg Points: ").append(stats.totalPoints / stats.userCount).append("\n\n");
            }
        }
        
        prompt.append("Generate 10 unique warning messages based on this data. Each message should:\n");
        prompt.append("- Compare groups (e.g., Silver vs Gold performance)\n");
        prompt.append("- Use actual numbers from the data\n");
        prompt.append("- Include emojis (⚠️, 🚨, ⚡, 🔴)\n");
        prompt.append("- Be urgent and actionable\n");
        prompt.append("- Vary vocabulary and phrasing\n\n");
        prompt.append("Return ONLY 10 messages, one per line, no numbering.");
        
        android.util.Log.d("BedrockWarning", "\n📤 ===== SENDING TO BEDROCK =====");
        android.util.Log.d("BedrockWarning", "Full Prompt:\n" + prompt.toString());
        android.util.Log.d("BedrockWarning", "================================\n");
        
        if (cloudWatchLogger != null) {
            cloudWatchLogger.logEvent("BEDROCK_WARNING_CALL", "promptLength=" + prompt.length());
        }
        
        bedrockService.sendMessage(prompt.toString(), response -> {
            android.util.Log.d("BedrockWarning", "📥 FULL Bedrock response: " + response);
            isGenerating = false;
            
            if (cloudWatchLogger != null) {
                cloudWatchLogger.logEvent("BEDROCK_WARNING_RESPONSE", "responseLength=" + response.length());
            }
            
            if (response.startsWith("❌") || response.contains("API Error")) {
                android.util.Log.e("BedrockWarning", "❌ Bedrock API error: " + response);
                callback.onMessagesGenerated(getFallbackMessages());
                return;
            }
            
            try {
                String[] lines = response.split("\n");
                List<String> messages = new ArrayList<>();
                for (String line : lines) {
                    line = line.trim();
                    line = line.replaceFirst("^\\d+\\.\\s*", "");
                    
                    if (!line.isEmpty() && line.length() > 10) {
                        messages.add(line);
                        android.util.Log.d("BedrockWarning", "✅ Added: " + line);
                    }
                }
                android.util.Log.d("BedrockWarning", "✅✅✅ PARSED " + messages.size() + " MESSAGES - CALLING CALLBACK NOW");
                if (messages.size() >= 5) {
                    cachedMessages = messages;
                    if (cloudWatchLogger != null) {
                        cloudWatchLogger.logEvent("WARNING_MESSAGES_GENERATED", "count=" + messages.size());
                    }
                    callback.onMessagesGenerated(messages);
                } else {
                    android.util.Log.w("BedrockWarning", "⚠️ Only " + messages.size() + " messages, using fallback");
                    callback.onMessagesGenerated(getFallbackMessages());
                }
            } catch (Exception e) {
                android.util.Log.e("BedrockWarning", "❌ Parse error: " + e.getMessage());
                e.printStackTrace();
                callback.onMessagesGenerated(getFallbackMessages());
            }
        });
    }
    
    public static void setCloudWatchLogger(CloudWatchLogger logger) {
        cloudWatchLogger = logger;
    }
    
    public static List<String> getWarningMessages() {
        if (cachedMessages.isEmpty()) {
            return getFallbackMessages();
        }
        return cachedMessages;
    }
    
    private static List<String> getFallbackMessages() {
        Random random = new Random();
        List<String> messages = new ArrayList<>();
        messages.add("⚠️ ALERT: Gold tier performance dropped " + (25 + random.nextInt(15)) + "% below expected standards");
        messages.add("🚨 CRITICAL: Silver group outperforming Gold - immediate tier review needed");
        messages.add("⚡ WARNING: Member's steps declined from " + (7000 + random.nextInt(1500)) + " to " + (6000 + random.nextInt(1000)) + " in 2 weeks");
        messages.add("🔴 URGENT: Gold tier sleep quality " + (20 + random.nextInt(15)) + "% below Silver average");
        messages.add("⚠️ RISK: Gold group at risk of demotion if current trends continue");
        return messages;
    }
}
