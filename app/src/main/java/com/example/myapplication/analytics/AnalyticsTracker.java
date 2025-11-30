package com.example.myapplication.analytics;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsTracker {
    
    private static FirebaseAnalytics analytics;
    private static CloudWatchLogger cloudWatch;
    
    public static void init(Context context) {
        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(context);
            cloudWatch = new CloudWatchLogger(context);
        }
    }
    
    public static void trackBedrockCall(String promptType, int responseLength) {
        Bundle params = new Bundle();
        params.putString("prompt_type", promptType);
        params.putInt("response_length", responseLength);
        params.putLong("timestamp", System.currentTimeMillis());
        analytics.logEvent("bedrock_api_call", params);
        
        // Also log to CloudWatch
        if (cloudWatch != null) {
            cloudWatch.logEvent("BEDROCK_CALL", "type=" + promptType + ",length=" + responseLength);
        }
    }
    
    public static void trackWithingsDataFetch(String dataType, boolean success) {
        Bundle params = new Bundle();
        params.putString("data_type", dataType);
        params.putBoolean("success", success);
        analytics.logEvent("withings_data_fetch", params);
    }
    
    public static void trackHealthMetric(String metric, double value) {
        Bundle params = new Bundle();
        params.putString("metric_name", metric);
        params.putDouble("metric_value", value);
        analytics.logEvent("health_metric_logged", params);
    }
    
    public static void trackScreen(String screenName) {
        Bundle params = new Bundle();
        params.putString("screen_name", screenName);
        analytics.logEvent("screen_view", params);
    }
}
