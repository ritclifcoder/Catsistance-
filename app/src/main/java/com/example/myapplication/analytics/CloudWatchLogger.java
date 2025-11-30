package com.example.myapplication.analytics;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AmazonCloudWatchLogsClient;
import com.amazonaws.services.logs.model.*;

import java.util.ArrayList;
import java.util.List;

public class CloudWatchLogger {
    private static final String TAG = "CloudWatchLogger";
    private static final String LOG_GROUP = "catsistanceapp";
    private static final String LOG_STREAM = "user-events";
    
    private AmazonCloudWatchLogsClient cloudWatchClient;
    private boolean isEnabled = false;
    private String sequenceToken = null;

    public CloudWatchLogger(Context context) {
        try {
            // TODO: Replace with your AWS credentials (use Cognito in production!)
            String accessKey = "AKIA2DNFYY2R757JNMS3";
            String secretKey = "xG3oPp54i/XoWuOZMMBw1dsOEjPalfSvdhXDmJ6e";
            
            if (!accessKey.equals("YOUR_ACCESS_KEY")) {
                BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                cloudWatchClient = new AmazonCloudWatchLogsClient(credentials);
                cloudWatchClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
                ensureLogGroupExists();
                isEnabled = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "CloudWatch init failed: " + e.getMessage());
        }
    }

    private void ensureLogGroupExists() {
        new Thread(() -> {
            try {
                // Create log group if it doesn't exist
                try {
                    cloudWatchClient.createLogGroup(new CreateLogGroupRequest(LOG_GROUP));
                    Log.d(TAG, "Created log group: " + LOG_GROUP);
                } catch (ResourceAlreadyExistsException e) {
                    Log.d(TAG, "Log group already exists");
                }

                // Create log stream if it doesn't exist
                try {
                    cloudWatchClient.createLogStream(new CreateLogStreamRequest(LOG_GROUP, LOG_STREAM));
                    Log.d(TAG, "Created log stream: " + LOG_STREAM);
                } catch (ResourceAlreadyExistsException e) {
                    Log.d(TAG, "Log stream already exists");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to ensure log group exists: " + e.getMessage());
            }
        }).start();
    }

    public void logEvent(String eventName, String eventData) {
        if (!isEnabled) {
            Log.d(TAG, "CloudWatch disabled - Event: " + eventName);
            return;
        }

        new Thread(() -> {
            try {
                InputLogEvent logEvent = new InputLogEvent()
                    .withTimestamp(System.currentTimeMillis())
                    .withMessage(eventName + ": " + eventData);

                List<InputLogEvent> logEvents = new ArrayList<>();
                logEvents.add(logEvent);

                PutLogEventsRequest request = new PutLogEventsRequest()
                    .withLogGroupName(LOG_GROUP)
                    .withLogStreamName(LOG_STREAM)
                    .withLogEvents(logEvents)
                    .withSequenceToken(sequenceToken);

                PutLogEventsResult result = cloudWatchClient.putLogEvents(request);
                sequenceToken = result.getNextSequenceToken();
                Log.d(TAG, "Event logged to CloudWatch: " + eventName);
            } catch (Exception e) {
                Log.e(TAG, "Failed to log event: " + e.getMessage());
            }
        }).start();
    }

    // Quick event methods
    public void logUserAction(String action) {
        logEvent("USER_ACTION", action);
    }

    public void logHealthData(String type, String value) {
        logEvent("HEALTH_DATA", type + "=" + value);
    }

    public void logRecommendation(String from, String to) {
        logEvent("RECOMMENDATION", "from=" + from + ",to=" + to);
    }

    public void logXPEarned(String userId, int xp) {
        logEvent("XP_EARNED", "user=" + userId + ",xp=" + xp);
    }
}
