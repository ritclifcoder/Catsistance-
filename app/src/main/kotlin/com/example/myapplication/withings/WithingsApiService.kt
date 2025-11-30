package com.example.myapplication.withings

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class WithingsApiService {
    private val client = OkHttpClient()
    
    companion object {
        private const val CLIENT_ID = "a7b3f425ca8fdc1233a2ddbb9616f385529d046b123ea70cad9c30c470bd60c9"
        private const val CLIENT_SECRET = "3ad1ecbd82d608fc7e6e9d4905a1b491751b27803bff2ab877bca8f8ec778c83"
        private const val TOKEN_URL = "https://wbsapi.withings.net/v2/oauth2"
        private const val MEASURE_URL = "https://wbsapi.withings.net/measure"
    }
    
    fun getAccessToken(authCode: String, redirectUri: String, callback: (String?, String?) -> Unit) {
        val formBody = FormBody.Builder()
            .add("action", "requesttoken")
            .add("grant_type", "authorization_code")
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .add("code", authCode)
            .add("redirect_uri", redirectUri)
            .build()
        
        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(formBody)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                android.util.Log.d("WithingsApiService", "Response: $body")
                if (response.isSuccessful && body != null) {
                    try {
                        val json = JSONObject(body)
                        if (json.has("body") && json.getJSONObject("body").has("access_token")) {
                            val accessToken = json.getJSONObject("body").getString("access_token")
                            callback(accessToken, null)
                        } else {
                            callback(null, "No access_token in response: $body")
                        }
                    } catch (e: Exception) {
                        callback(null, "Parse error: ${e.message}")
                    }
                } else {
                    callback(null, "Error: ${response.code} - $body")
                }
            }
        })
    }
    
    fun sendHealthData(accessToken: String, steps: Int, systolic: Int, diastolic: Int, callback: (Boolean, String?) -> Unit) {
        val timestamp = System.currentTimeMillis() / 1000
        
        val formBody = FormBody.Builder()
            .add("action", "measure")
            .add("meastype", "11")
            .add("value", steps.toString())
            .add("date", timestamp.toString())
            .build()
        
        val request = Request.Builder()
            .url("https://wbsapi.withings.net/measure?action=measure")
            .addHeader("Authorization", "Bearer $accessToken")
            .post(formBody)
            .build()
        
        android.util.Log.d("WithingsApiService", "Sending steps: $steps")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("WithingsApiService", "Send failed: ${e.message}")
                callback(false, e.message)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                android.util.Log.d("WithingsApiService", "Send response: $body")
                callback(response.isSuccessful, body)
            }
        })
    }
}
