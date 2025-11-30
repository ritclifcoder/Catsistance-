# AWS CloudWatch Setup Guide for Catsistance

## Step 1: Create CloudWatch Log Group

1. Go to AWS Console → CloudWatch
2. Click "Logs" → "Log groups"
3. Click "Create log group"
4. Name: `catsistance-app`
5. Click "Create"

## Step 2: Create Log Stream

1. Click on `catsistance-app` log group
2. Click "Create log stream"
3. Name: `user-events`
4. Click "Create"

## Step 3: Add CloudWatch Permission to Existing IAM User

1. Go to AWS Console → IAM
2. Click "Users" → Select your Bedrock user
3. Click "Add permissions" → "Attach policies directly"
4. Search and select: `CloudWatchLogsFullAccess`
5. Click "Add permissions"

## Step 4: Use Existing Credentials

Bedrock için kullandığın aynı credentials'ları kullan:
- Access key ID (zaten var)
- Secret access key (zaten var)

## Step 5: Update CloudWatchLogger.java

Replace lines 27-28 in CloudWatchLogger.java:

```java
String accessKey = "YOUR_ACCESS_KEY_HERE";
String secretKey = "YOUR_SECRET_KEY_HERE";
```

## Step 6: Test

Run your app and check CloudWatch Logs to see events appearing!

## ⚠️ Security Note

For production, use AWS Cognito instead of hardcoded credentials!
