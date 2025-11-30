package com.example.myapplication.Notification;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SendNotification {
    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;
    
    private static final String postUrl = "https://fcm.googleapis.com/v1/projects/englishdic-80c3a/messages:send";
    
    public SendNotification(String userFcmToken, String title, String body, Context context) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }
    
    public void SendNotifications() {
        Log.d("SendNotification", "\n--- SENDING NOTIFICATION ---");
        Log.d("SendNotification", "Title: " + title);
        Log.d("SendNotification", "Body: " + body.substring(0, Math.min(50, body.length())) + "...");
        Log.d("SendNotification", "Token: " + userFcmToken.substring(0, Math.min(20, userFcmToken.length())) + "...");
        
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        
        try {
            JSONObject messageObject = new JSONObject();
            
            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("body", body);
            
            JSONObject androidObject = new JSONObject();
            androidObject.put("priority", "high");
            
            messageObject.put("token", userFcmToken);
            messageObject.put("data", dataObject);
            messageObject.put("android", androidObject);
            
            mainObj.put("message", messageObject);
            Log.d("SendNotification", "✅ JSON payload created");
            
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response -> {
                Log.d("SendNotification", "✅ SUCCESS - FCM Response: " + response.toString());
            }, volleyError -> {
                Log.e("SendNotification", "❌ FAILED - Volley Error");
                
                if (volleyError.networkResponse != null) {
                    int statusCode = volleyError.networkResponse.statusCode;
                    String errorData = new String(volleyError.networkResponse.data);
                    
                    Log.e("SendNotification", "HTTP Status: " + statusCode);
                    Log.e("SendNotification", "Error Response: " + errorData);
                    
                    if (statusCode == 401) {
                        Log.e("SendNotification", "❌ AUTHENTICATION FAILED - Check access token");
                    } else if (statusCode == 404) {
                        Log.e("SendNotification", "❌ INVALID FCM TOKEN - Token may be expired");
                    }
                    
                    Toast.makeText(context, "Error " + statusCode + ": " + errorData, Toast.LENGTH_LONG).show();
                } else {
                    Log.e("SendNotification", "Network Error: " + volleyError.toString());
                    Log.e("SendNotification", "Cause: " + (volleyError.getCause() != null ? volleyError.getCause().getMessage() : "Unknown"));
                    Toast.makeText(context, "Network Error: " + volleyError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Log.d("SendNotification", "Fetching access token...");
                    AccessToken accessToken = new AccessToken();
                    String accessKey = accessToken.getAccessToken();
                    
                    if (accessKey != null) {
                        Log.d("SendNotification", "✅ Access token obtained: " + accessKey.substring(0, Math.min(20, accessKey.length())) + "...");
                    } else {
                        Log.e("SendNotification", "❌ Access token is NULL!");
                    }
                    
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer " + accessKey);
                    return header;
                }
            };
            
            Log.d("SendNotification", "Adding request to queue...");
            requestQueue.add(request);
            Log.d("SendNotification", "✅ Request queued successfully");
            
        } catch (JSONException e) {
            Log.e("SendNotification", "❌ JSON Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
