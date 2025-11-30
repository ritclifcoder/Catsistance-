package com.example.myapplication.WithingAPI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;

public class WithingsAuthActivity extends AppCompatActivity {
    
    private static final String CLIENT_ID = "a7b3f425ca8fdc1233a2ddbb9616f385529d046b123ea70cad9c30c470bd60c9";
    private static final String REDIRECT_URI = "myapp://withings/callback";
    private static final String AUTH_URL = "https://account.withings.com/oauth2_user/authorize2";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        
        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        layout.addView(progressBar);
        
        android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText("Getting demo account access...");
        textView.setTextSize(16);
        textView.setPadding(0, 30, 0, 0);
        textView.setGravity(Gravity.CENTER);
        layout.addView(textView);
        
        setContentView(layout);
        
        getDemoAccess();
    }
    
    private void getDemoAccess() {
        new Thread(() -> {
            try {
                String state = String.valueOf(System.currentTimeMillis());
                getSharedPreferences("WithingsPrefs", MODE_PRIVATE).edit().putString("oauth_state", state).apply();
                
                String authUrl = AUTH_URL + "?response_type=code" +
                        "&client_id=" + CLIENT_ID +
                        "&redirect_uri=" + android.net.Uri.encode(REDIRECT_URI) +
                        "&scope=user.metrics,user.activity" +
                        "&state=" + state;
                
                runOnUiThread(() -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                    startActivity(browserIntent);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
    
    private String generateSignature(String nonce) {
        try {
            String data = "action=getdemoaccess&client_id=" + CLIENT_ID + "&nonce=" + nonce;
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            handleCallback(data);
        }
    }
    
    private void handleCallback(Uri uri) {
        String code = uri.getQueryParameter("code");
        String state = uri.getQueryParameter("state");
        String savedState = getSharedPreferences("WithingsPrefs", MODE_PRIVATE).getString("oauth_state", null);
        
        if (code != null && state != null && state.equals(savedState)) {
            exchangeCodeForToken(code);
        } else {
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void exchangeCodeForToken(String code) {
        WithingsTokenManager.getInstance(this).exchangeCodeForToken(code, new WithingsTokenManager.TokenCallback() {
            @Override
            public void onSuccess(String accessToken) {
                runOnUiThread(() -> {
                    Toast.makeText(WithingsAuthActivity.this, "Withings connected successfully!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(WithingsAuthActivity.this, "Connection failed: " + error, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }
        });
    }
}
