package com.example.myapplication.WithingAPI;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import com.example.myapplication.analytics.CloudWatchLogger;
import android.content.Context;

public class WithingsClient {
    private static final String BASE_URL = "https://wbsapi.withings.net/";
    private static WithingsClient instance;
    private WithingsApiService apiService;
    private static CloudWatchLogger cloudWatchLogger;
    
    private WithingsClient() {
        setupRetrofit();
    }
    
    public static WithingsClient getInstance() {
        if (instance == null) {
            instance = new WithingsClient();
        }
        return instance;
    }
    
    public static void setCloudWatchLogger(CloudWatchLogger logger) {
        cloudWatchLogger = logger;
    }
    
    private void setupRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(WithingsApiService.class);
    }
    
    public WithingsApiService getApiService() {
        return apiService;
    }
    
    public void getBloodPressure(String accessToken, HealthDataCallback callback) {
        String auth = "Bearer " + accessToken;
        long endDate = System.currentTimeMillis() / 1000;
        long startDate = endDate - (30 * 24 * 60 * 60);
        apiService.getHealthData(auth, "getmeas", "9,10", "1", startDate, endDate).enqueue(new retrofit2.Callback<WithingsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<WithingsResponse> call, retrofit2.Response<WithingsResponse> response) {
                android.util.Log.d("WithingsClient", "BP Response: " + response.isSuccessful());
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_BP_RESPONSE", "success=" + response.isSuccessful() + ",code=" + response.code());
                }
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("WithingsClient", "BP Body: " + (response.body().getBody() != null ? "exists" : "null"));
                    HealthData data = response.body().toHealthData();
                    android.util.Log.d("WithingsClient", "BP Parsed: " + (data.getBloodPressure() != null ? data.getBloodPressure().toString() : "null"));
                    
                    if (data.getBloodPressure() != null && data.getBloodPressure().getSystolic() > 0) {
                        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            com.google.firebase.database.FirebaseDatabase db = com.google.firebase.database.FirebaseDatabase.getInstance("https://cat-sistance-default-rtdb.europe-west1.firebasedatabase.app/");
                            db.getReference("Users").child(userId).child("vitals").child("systolic").setValue(data.getBloodPressure().getSystolic());
                            db.getReference("Users").child(userId).child("vitals").child("diastolic").setValue(data.getBloodPressure().getDiastolic());
                            int heartRate = data.getHeartRate() > 0 ? data.getHeartRate() : 72;
                            db.getReference("Users").child(userId).child("vitals").child("heartRate").setValue(heartRate);
                            android.util.Log.d("WithingsClient", "✅ [" + userId + "] BP & HR written: " + data.getBloodPressure().toString() + ", HR: " + heartRate);
                            if (cloudWatchLogger != null) {
                                cloudWatchLogger.logEvent("WITHINGS_BP_DATA", "systolic=" + data.getBloodPressure().getSystolic() + ",diastolic=" + data.getBloodPressure().getDiastolic() + ",hr=" + heartRate);
                            }
                        }
                    }
                    
                    callback.onSuccess(data);
                } else {
                    android.util.Log.e("WithingsClient", "BP Failed: " + response.code());
                    callback.onError("Failed to fetch blood pressure");
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<WithingsResponse> call, Throwable t) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_BP_ERROR", "error=" + t.getMessage());
                }
                callback.onError(t.getMessage());
            }
        });
    }
    
    public void getSteps(String accessToken, String startDate, String endDate, HealthDataCallback callback) {
        String auth = "Bearer " + accessToken;
        long endTimestamp = System.currentTimeMillis() / 1000;
        long startTimestamp = endTimestamp - (30 * 24 * 60 * 60);
        apiService.getHealthData(auth, "getmeas", "36", "1", startTimestamp, endTimestamp).enqueue(new retrofit2.Callback<WithingsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<WithingsResponse> call, retrofit2.Response<WithingsResponse> response) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_STEPS_RESPONSE", "success=" + response.isSuccessful() + ",code=" + response.code());
                }
                if (response.isSuccessful() && response.body() != null) {
                    HealthData data = response.body().toHealthData();
                    if (cloudWatchLogger != null && data.getStepsToday() > 0) {
                        cloudWatchLogger.logEvent("WITHINGS_STEPS_DATA", "steps=" + data.getStepsToday());
                    }
                    callback.onSuccess(data);
                } else {
                    callback.onError("Failed to fetch steps");
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<WithingsResponse> call, Throwable t) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_STEPS_ERROR", "error=" + t.getMessage());
                }
                callback.onError(t.getMessage());
            }
        });
    }
    

    public void getWeight(String accessToken, HealthDataCallback callback) {
        String auth = "Bearer " + accessToken;
        long endDate = System.currentTimeMillis() / 1000;
        long startDate = endDate - (30 * 24 * 60 * 60);
        apiService.getHealthData(auth, "getmeas", "1", "1", startDate, endDate).enqueue(new retrofit2.Callback<WithingsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<WithingsResponse> call, retrofit2.Response<WithingsResponse> response) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_WEIGHT_RESPONSE", "success=" + response.isSuccessful() + ",code=" + response.code());
                }
                if (response.isSuccessful() && response.body() != null) {
                    HealthData data = response.body().toHealthData();
                    if (cloudWatchLogger != null && data.getWeight() > 0) {
                        cloudWatchLogger.logEvent("WITHINGS_WEIGHT_DATA", "weight=" + data.getWeight());
                    }
                    callback.onSuccess(data);
                } else {
                    callback.onError("Failed to fetch weight");
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<WithingsResponse> call, Throwable t) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("WITHINGS_WEIGHT_ERROR", "error=" + t.getMessage());
                }
                callback.onError(t.getMessage());
            }
        });
    }
    
    public interface HealthDataCallback {
        void onSuccess(HealthData data);
        void onError(String error);
    }
}