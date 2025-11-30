package com.example.myapplication.withings

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WithingsAuthActivity : AppCompatActivity() {
    
    private val apiService = WithingsApiService()
    private lateinit var webView: WebView
    private var isProcessingCallback = false
    
    companion object {
        private const val CLIENT_ID = "a7b3f425ca8fdc1233a2ddbb9616f385529d046b123ea70cad9c30c470bd60c9"
        private const val REDIRECT_URI = "myapp://withings/callback"
        private const val AUTH_URL = "https://account.withings.com/oauth2_user/authorize2"
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        setContentView(webView)
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                android.util.Log.d("WithingsAuth", "URL loading: $url")
                if (url?.startsWith(REDIRECT_URI) == true) {
                    android.util.Log.d("WithingsAuth", "Callback detected!")
                    handleCallback(Uri.parse(url))
                    return true
                }
                return false
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                android.util.Log.d("WithingsAuth", "Page finished: $url")
                if (url?.startsWith(REDIRECT_URI) == true) {
                    handleCallback(Uri.parse(url))
                }
            }
        }
        
        val state = java.util.UUID.randomUUID().toString()
        val authUrl = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "user.metrics")
            .appendQueryParameter("state", state)
            .build()
        
        webView.loadUrl(authUrl.toString())
    }
    
    private fun handleCallback(uri: Uri) {
        if (isProcessingCallback) return
        isProcessingCallback = true
        
        android.util.Log.d("WithingsAuth", "handleCallback: $uri")
        val code = uri.getQueryParameter("code")
        android.util.Log.d("WithingsAuth", "Authorization code: $code")
        
        if (code != null) {
            Toast.makeText(this, "Getting access token...", Toast.LENGTH_SHORT).show()
            apiService.getAccessToken(code, REDIRECT_URI) { token, error ->
                runOnUiThread {
                    if (token != null) {
                        android.util.Log.d("WithingsAuth", "Token received: $token")
                        saveToken(token)
                        Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        android.util.Log.e("WithingsAuth", "Token error: $error")
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            android.util.Log.e("WithingsAuth", "No code in callback URL")
        }
    }
    
    private fun saveToken(token: String) {
        com.example.myapplication.WithingAPI.WithingsTokenManager.getInstance(this)
            .saveTokens(token, "")
    }
}
