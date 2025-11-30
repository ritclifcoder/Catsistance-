package com.example.myapplication.AmazonBedrockService;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BedrockService {
    private static final String TAG = "BedrockService";
    private final OkHttpClient client;
    private static final String BEDROCK_ENDPOINT = "https://bedrock-runtime.eu-central-1.amazonaws.com";
    private static final String MODEL_ID = "amazon.titan-text-lite-v1";

    public interface ResponseCallback {
        void onResponse(String response);
    }

    public BedrockService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void sendMessage(String message, ResponseCallback callback) {
        new Thread(() -> {
            try {
                String enhancedPrompt = "IMPORTANT: Be creative and use varied language. Never repeat the same phrases. Provide unique responses each time.\n\nUser question: " + message;
                String response = callBedrockAPI(enhancedPrompt);
                callback.onResponse(response);
            } catch (Exception e) {
                Log.e(TAG, "Bedrock API call failed", e);
                callback.onResponse("❌ API Error: " + e.getMessage());
            }
        }).start();
    }


    public void analyzeHealthData(List<String> healthInputs, ResponseCallback callback) {
        new Thread(() -> {
            try {
                StringBuilder prompt = new StringBuilder();
                prompt.append("Analyze each health metric and provide a personalized assessment with actionable tips.\n\n");
                prompt.append("IMPORTANT: Use creative and varied language. Never repeat the same phrases. Be unique and engaging each time.\n\n");
                
                for (int i = 0; i < healthInputs.size(); i++) {
                    prompt.append(healthInputs.get(i)).append("\n");
                }
                
                prompt.append("\nFor each metric above, create a JSON object with:\n");
                prompt.append("- input: the metric name\n");
                prompt.append("- status: \"normal\" or \"warning\"\n");
                prompt.append("- score: 0-100\n");
                prompt.append("- message: detailed assessment (2-3 sentences with specific health advice). USE DIFFERENT WORDS AND PHRASES EACH TIME. Be creative, motivating, and vary your vocabulary.\n\n");
                prompt.append("Return as JSON array. Example message: \"Your blood pressure of 120/80 is in the healthy range. Keep it optimal by doing 30 minutes of cardio 5 times weekly. Limit sodium intake to under 2,300mg per day.\"\n\n");
                prompt.append("Return ONLY the JSON array with all 4 metrics analyzed.");
                
                String response = callBedrockAPI(prompt.toString());
                callback.onResponse(response);
            } catch (Exception e) {
                Log.e(TAG, "Health analysis failed", e);
                callback.onResponse("{\"error\":\"Analysis failed\"}");
            }
        }).start();
    }
    
    public void assessGroupRankings(ResponseCallback callback) {
        new Thread(() -> {
            try {
                List<GroupRankingSystem.Group> groups = GroupRankingSystem.getMockData();
                GroupRankingSystem.Assessment result = GroupRankingSystem.assessGroups(groups);
                
                StringBuilder response = new StringBuilder();
                response.append("📊 GROUP DYNAMICS ASSESSMENT\n\n");
                response.append(result.summary).append("\n\n");
                
                if (!result.suggestions.isEmpty()) {
                    response.append("💡 RECOMMENDED ACTIONS:\n");
                    for (String suggestion : result.suggestions) {
                        response.append("• ").append(suggestion).append("\n");
                    }
                }
                
                callback.onResponse(response.toString());
            } catch (Exception e) {
                Log.e(TAG, "Assessment failed", e);
                callback.onResponse("Assessment failed");
            }
        }).start();
    }
    
    private String generateAdvancedResponse(String message) {
        String msg = message.toLowerCase();
        
        if (msg.contains("group") && (msg.contains("assess") || msg.contains("ranking"))) {
            return "📊 Running group dynamics assessment...";
        } else if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "🤖 Hello! I'm your AI health assistant powered by Amazon Bedrock. I can help you with health insights, wellness tips, and answer questions about your fitness data. What would you like to know?";
        } else if (msg.contains("health") || msg.contains("fitness")) {
            return "🏥 I can analyze your health data and provide personalized recommendations. Based on your current metrics, I suggest maintaining regular exercise and monitoring your vital signs. Would you like specific advice about any health aspect?";
        } else if (msg.contains("blood pressure")) {
            return "🩺 Your blood pressure readings look good! To maintain healthy levels, I recommend: regular cardio exercise, reducing sodium intake, managing stress, and getting adequate sleep. Keep monitoring daily!";
        } else if (msg.contains("steps") || msg.contains("walking")) {
            return "🚶 Great job on staying active! Your step count shows good daily movement. To optimize your walking routine, try: setting daily goals, taking stairs when possible, and incorporating short walks after meals.";
        } else if (msg.contains("sleep")) {
            return "😴 Quality sleep is crucial for health! For better sleep: maintain consistent bedtime, avoid screens 1 hour before bed, keep room cool and dark, and try relaxation techniques. Aim for 7-9 hours nightly.";
        } else if (msg.contains("water") || msg.contains("hydration")) {
            return "💧 Staying hydrated is excellent! Your water intake looks good. Remember: drink water throughout the day, increase intake during exercise, and monitor urine color as a hydration indicator.";
        } else if (msg.contains("how are you")) {
            return "😊 I'm functioning optimally and ready to help! As your AI health companion, I'm here 24/7 to provide insights, answer questions, and support your wellness journey. What can I assist you with today?";
        } else {
            return "🤖 I understand you're asking about: '" + message + "'. As your AI health assistant, I can help with fitness tracking, nutrition advice, wellness tips, and health data analysis. Could you be more specific about what health topic interests you?";
        }
    }

    private String callBedrockAPI(String message) throws Exception {
        String url = BEDROCK_ENDPOINT + "/model/amazon.titan-text-express-v1/invoke";
        
        JSONObject textGenerationConfig = new JSONObject();
        textGenerationConfig.put("maxTokenCount", 512);
        textGenerationConfig.put("temperature", 0.9);
        textGenerationConfig.put("topP", 0.95);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("inputText", message);
        requestBody.put("textGenerationConfig", textGenerationConfig);

        String jsonPayload = requestBody.toString();
        
        RequestBody body = RequestBody.create(
            jsonPayload.getBytes("UTF-8"),
            MediaType.parse("application/json")
        );

        // Use AWSConfig for proper authentication
        java.util.Map<String, String> headers = AWSConfig.getAuthHeaders(jsonPayload);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        
        // Add all auth headers
        for (java.util.Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        
        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            String responseBodyStr = response.body().string();
            Log.d(TAG, "Response code: " + response.code());
            Log.d(TAG, "Response body: " + responseBodyStr);
            
            if (!response.isSuccessful()) {
                throw new IOException("API Error: " + response.code() + " - " + responseBodyStr);
            }
            
            JSONObject jsonResponse = new JSONObject(responseBodyStr);
            
            if (jsonResponse.has("results")) {
                return jsonResponse.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("outputText");
            } else {
                return "No response content found";
            }
        }
    }


}