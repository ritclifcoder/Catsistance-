# Withings OAuth Setup Guide

## Step 1: Get Withings API Credentials

1. Go to https://developer.withings.com/
2. Create a developer account
3. Create a new application
4. Note your **Client ID** and **Client Secret**
5. Set redirect URI to: `myapp://withings/callback`

## Step 2: Update Configuration

Update these files with your credentials:

### WithingsAuthActivity.java
```java
private static final String CLIENT_ID = "YOUR_CLIENT_ID_HERE";
```

### WithingsTokenManager.java
```java
private static final String CLIENT_ID = "YOUR_CLIENT_ID_HERE";
private static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET_HERE";
```

## Step 3: How It Works

1. **First Launch**: When user opens DashboardActivity, if not authenticated, OAuth flow starts
2. **WebView Opens**: User logs into Withings account
3. **Authorization**: User grants permissions (user.metrics, user.activity)
4. **Token Exchange**: App exchanges authorization code for access token
5. **Token Storage**: Access token saved in SharedPreferences
6. **API Calls**: Use token to fetch real health data

## Step 4: Test Authentication

Run the app and:
- Open DashboardActivity
- WebView should open with Withings login
- Login with your Withings account
- Grant permissions
- You'll be redirected back to the app
- Toast message: "Connected to Withings!"

## Next Steps

After authentication works:
- Integrate real API calls in DashboardActivity
- Replace mock data with WithingsClient methods
- Handle token refresh when expired
