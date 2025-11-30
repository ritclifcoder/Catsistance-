package com.example.myapplication.WithingAPI;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WithingsTokenManager {
    
    private static final String PREFS_NAME = "WithingsPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token_";
    private static final String KEY_REFRESH_TOKEN = "refresh_token_";
    private String userId;
    private static final String CLIENT_ID = "a7b3f425ca8fdc1233a2ddbb9616f385529d046b123ea70cad9c30c470bd60c9";
    private static final String CLIENT_SECRET = "b3c2562fc882478fc8a87f7635088918c4b9d7804e4579eec9f4be9856a92618";
    private static final String REDIRECT_URI = "myapp://withings/callback";
    private static final String TOKEN_URL = "https://wbsapi.withings.net/v2/oauth2";
    
    private static WithingsTokenManager instance;
    private SharedPreferences prefs;
    private OkHttpClient client;
    
    private WithingsTokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        client = new OkHttpClient();
        this.userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                      com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }
    
    public static WithingsTokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new WithingsTokenManager(context);
        }
        return instance;
    }
    
    public void exchangeCodeForToken(String code, TokenCallback callback) {
        android.util.Log.d("WithingsTokenManager", "🔄 Starting token exchange for code: " + code.substring(0, 10) + "...");
        
        FormBody body = new FormBody.Builder()
                .add("action", "requesttoken")
                .add("grant_type", "authorization_code")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .build();
        
        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();
        
        android.util.Log.d("WithingsTokenManager", "📡 Sending token request to: " + TOKEN_URL);
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("WithingsTokenManager", "❌ Token exchange failed: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    android.util.Log.d("WithingsTokenManager", "📥 Token response: " + responseBody);
                    
                    JSONObject json = new JSONObject(responseBody);
                    
                    if (json.has("status") && json.getInt("status") != 0) {
                        String error = "API error: status " + json.getInt("status");
                        android.util.Log.e("WithingsTokenManager", "❌ " + error);
                        callback.onError(error);
                        return;
                    }
                    
                    JSONObject bodyObj = json.getJSONObject("body");
                    
                    String accessToken = bodyObj.getString("access_token");
                    String refreshToken = bodyObj.getString("refresh_token");
                    
                    android.util.Log.d("WithingsTokenManager", "✅ Tokens received successfully");
                    saveTokens(accessToken, refreshToken);
                    callback.onSuccess(accessToken);
                } catch (Exception e) {
                    android.util.Log.e("WithingsTokenManager", "❌ Token parse error: " + e.getMessage());
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }
    
    public void refreshAccessToken(TokenCallback callback) {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            callback.onError("No refresh token available");
            return;
        }
        
        FormBody body = new FormBody.Builder()
                .add("action", "requesttoken")
                .add("grant_type", "refresh_token")
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("refresh_token", refreshToken)
                .build();
        
        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    JSONObject bodyObj = json.getJSONObject("body");
                    
                    String accessToken = bodyObj.getString("access_token");
                    String newRefreshToken = bodyObj.getString("refresh_token");
                    
                    saveTokens(accessToken, newRefreshToken);
                    callback.onSuccess(accessToken);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    public void saveTokens(String accessToken, String refreshToken) {
        if (userId == null) return;
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN + userId, accessToken)
                .putString(KEY_REFRESH_TOKEN + userId, refreshToken)
                .apply();
        
        // Also save to Firebase
        com.google.firebase.database.FirebaseDatabase.getInstance("https://cat-sistance-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Users").child(userId).child("withingsToken")
            .setValue(accessToken);
    }
    
    public String getAccessToken() {
        if (userId == null) return null;
        return prefs.getString(KEY_ACCESS_TOKEN + userId, null);
    }
    
    public String getRefreshToken() {
        if (userId == null) return null;
        return prefs.getString(KEY_REFRESH_TOKEN + userId, null);
    }
    
    public boolean isAuthenticated() {
        return getAccessToken() != null;
    }
    
    public void clearTokens() {
        if (userId == null) return;
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN + userId)
            .remove(KEY_REFRESH_TOKEN + userId)
            .apply();
    }
    
    public interface TokenCallback {
        void onSuccess(String accessToken);
        void onError(String error);
    }
}
