# 2-Minute Notification Debug Guide

## Logcat Filters to Use

### 1. View All Notification Logs
```
adb logcat -s NotificationScheduler:* SendNotification:* MainActivity:* FCM:*
```

### 2. View Only Errors
```
adb logcat *:E
```

### 3. View Scheduler Lifecycle
```
adb logcat | findstr "INITIALIZING CANCELLING EXECUTION"
```

## Expected Log Flow

### When App Starts (MainActivity.onCreate)
```
MainActivity: FCM Token: [token_value]
MainActivity: FCM token saved and notifications scheduled

NotificationScheduler: ========================================
NotificationScheduler: INITIALIZING NOTIFICATION SCHEDULER
NotificationScheduler: Interval: 120 seconds (2 minutes)
NotificationScheduler: ✅ Handler created on main looper
NotificationScheduler: 🚀 Sending FIRST notification immediately...
NotificationScheduler: === STARTING NOTIFICATION BATCH ===
NotificationScheduler: ✅ Firebase query successful
NotificationScheduler: Total users in database: X
NotificationScheduler: Processing user: [userId]
NotificationScheduler: ✅ Notification sent to: [userId]
NotificationScheduler: === BATCH COMPLETE ===
NotificationScheduler: Sent: X, Skipped: Y
NotificationScheduler: ✅ Scheduler active: First sent now, next in 2 minutes
```

### Every 2 Minutes
```
NotificationScheduler: --- EXECUTION #2 ---
NotificationScheduler: Time: 14:32:15
NotificationScheduler: === STARTING NOTIFICATION BATCH ===
[... same as above ...]
NotificationScheduler: Scheduling next execution in 120 seconds...
```

### When Notification Sends
```
SendNotification: --- SENDING NOTIFICATION ---
SendNotification: Title: 🏆 Group Performance Alert
SendNotification: Body: [message]...
SendNotification: Token: [token_prefix]...
SendNotification: ✅ JSON payload created
SendNotification: Fetching access token...
SendNotification: ✅ Access token obtained: [token_prefix]...
SendNotification: ✅ Request queued successfully
SendNotification: ✅ SUCCESS - FCM Response: {...}
```

### When App Closes (MainActivity.onDestroy)
```
NotificationScheduler: ========================================
NotificationScheduler: CANCELLING NOTIFICATION SCHEDULER
NotificationScheduler: ✅ Handler callbacks removed
```

## Common Issues & Solutions

### Issue 1: No logs appear
**Problem:** Scheduler not starting
**Check:**
- Is MainActivity.saveFCMTokenAndStartNotifications() being called?
- Is Firebase Auth user logged in?
- Check: `adb logcat | findstr "FCM token saved"`

### Issue 2: "Failed to fetch users"
**Problem:** Firebase Database permission denied
**Solution:**
```json
// Firebase Realtime Database Rules
{
  "rules": {
    "users": {
      ".read": true,
      ".write": true
    }
  }
}
```

### Issue 3: "Access token is NULL"
**Problem:** Google credentials failing
**Check:**
- Is google-auth-library-oauth2-http dependency added?
- Check AccessToken.java service account credentials
- Look for: `SendNotification: ❌ Access token is NULL!`

### Issue 4: HTTP 401 Error
**Problem:** Invalid or expired access token
**Logs:**
```
SendNotification: HTTP Status: 401
SendNotification: ❌ AUTHENTICATION FAILED
```
**Solution:** Verify service account JSON in AccessToken.java

### Issue 5: HTTP 404 Error
**Problem:** Invalid FCM token
**Logs:**
```
SendNotification: HTTP Status: 404
SendNotification: ❌ INVALID FCM TOKEN
```
**Solution:** User needs to reinstall app or refresh token

### Issue 6: Notifications stop after 2 minutes
**Problem:** Handler stops when app is killed
**Solution:** Use WorkManager (see NotificationSchedulerFixed.java)
**Note:** Handler only works while app is in foreground/background, not when killed

### Issue 7: "Skipped (no token)"
**Problem:** User doesn't have FCM token saved
**Check Firebase Database:**
```
users/
  [userId]/
    fcmToken: "should_exist_here"
```

## Testing Commands

### 1. Clear app data and restart
```
adb shell pm clear com.example.myapplication
adb shell am start -n com.example.myapplication/.SplashScreen
```

### 2. Monitor logs in real-time
```
adb logcat -c && adb logcat -s NotificationScheduler:* SendNotification:*
```

### 3. Check if Handler is running
```
adb logcat | findstr "EXECUTION #"
```

### 4. Force stop app (test Handler cancellation)
```
adb shell am force-stop com.example.myapplication
```

## Important Notes

1. **Handler Limitation:** Current implementation uses Handler which:
   - ✅ Works when app is in foreground
   - ✅ Works when app is in background
   - ❌ STOPS when app is killed by user or system

2. **For Production:** Use WorkManager (NotificationSchedulerFixed.java):
   - ✅ Survives app restarts
   - ✅ Survives device reboots
   - ⚠️ Minimum interval is 15 minutes (Android limitation)

3. **2-Minute Testing:** Only works with Handler while app is running

4. **Battery Optimization:** Some devices may kill background processes aggressively

## Quick Debug Checklist

- [ ] App starts successfully
- [ ] User is logged in (Firebase Auth)
- [ ] FCM token is obtained and saved
- [ ] "INITIALIZING NOTIFICATION SCHEDULER" appears in logs
- [ ] First notification batch executes immediately
- [ ] "EXECUTION #2" appears after 2 minutes
- [ ] SendNotification shows "✅ SUCCESS"
- [ ] No "❌" error symbols in logs
- [ ] Firebase Database has users with fcmToken field
- [ ] Access token is not NULL
- [ ] No 401/404 HTTP errors

## Logcat Color Coding

- ✅ = Success (look for these)
- ⚠️ = Warning (investigate if many)
- ❌ = Error (must fix)
- 🚀 = Important milestone
