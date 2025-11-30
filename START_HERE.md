# 🎉 Health Recommendation System - START HERE

## 📋 What Was Built

A complete health recommendation system where users can:
- ✅ Send health tips to group members
- ✅ Earn 10 XP per recommendation
- ✅ Limited to 3 recommendations per user
- ✅ View recommendation history
- ✅ Track total XP points

## 🚀 Quick Start (3 Steps)

### Step 1: Build the Project
```
1. Open Android Studio
2. Click: Build → Rebuild Project
3. Wait for build to complete
```

### Step 2: Set Up Firebase (if needed)
```
1. Open Firebase Console
2. Go to Realtime Database
3. Copy rules from FIREBASE_RULES.json
4. Paste into Rules tab
5. Click "Publish"
```

### Step 3: Test the App
```
1. Run the app
2. Login/Signup
3. Navigate to UsersActivity (view a user card)
4. Click "Recommend Health Activity" button
5. Select a health tip
6. See congratulations screen!
```

## 📁 Important Files

### Documentation (Read These First)
1. **START_HERE.md** ← You are here
2. **IMPLEMENTATION_SUMMARY.md** - Complete overview
3. **QUICK_REFERENCE.md** - Quick commands and paths
4. **DEPLOYMENT_CHECKLIST.md** - Testing checklist

### Guides
- **INTEGRATION_GUIDE.md** - How to integrate
- **RECOMMENDATION_SYSTEM_README.md** - Feature details
- **SYSTEM_DIAGRAM.txt** - Visual architecture

### Code Files Created
```
Activities:
├── ProfileActivity.java
├── RecommendationOptionsActivity.java
├── CongratulationsActivity.java
└── RecommendationHistoryActivity.java

Utilities:
├── RecommendationManager.java
└── TestDataSeeder.java

Layouts:
├── activity_profile.xml
├── activity_recommendation_options.xml
├── activity_congratulations.xml
├── activity_recommendation_history.xml
└── item_recommendation.xml
```

### Modified Files
```
├── AndroidManifest.xml (added 4 activities)
├── drawer_menu.xml (added 2 menu items)
├── DashboardActivity.java (added navigation)
└── SignUpActivity.java (added initialization)
```

## 🎯 Key Features

### 1. Recommend Health Button
- Located in ProfileActivity
- Shows remaining recommendations
- Checks if user is in a group

### 2. Four Health Options
- 💧 Drink more water
- 🚶 Walk more
- ❤️ Lower your blood pressure
- 😌 Reduce stress levels

### 3. XP System
- 10 XP per recommendation
- Stored in Firebase: `users/{userId}/totalPoints`
- Displayed in Profile and Congratulations screens

### 4. Recommendation Limit
- Maximum 3 per user
- Tracked in: `users/{userId}/recommendationsLeft`
- Shows countdown: "2 left", "1 left", "0 left"

### 5. Group-Only Sending
- Only sends to users in same group
- Excludes sender from recipients
- Requires user to be in a group

### 6. Recommendation History
- Shows all sent recommendations
- Displays: recipient, message, timestamp
- Accessible from Profile or drawer menu

## 🗄️ Firebase Structure

```
users/
  {userId}/
    totalPoints: 150          ← XP points
    recommendationsLeft: 2    ← Remaining (0-3)
    groupId: "group123"       ← User's group

groups/
  {groupId}/
    members/
      {userId1}: true
      {userId2}: true

recommendations/
  {recId}/
    from: "userId1"           ← Sender
    to: "userId2"             ← Recipient
    message: "Drink water"    ← Health tip
    timestamp: 1234567890     ← When sent
```

## 🧪 Testing Instructions

### Test Flow 1: Send Recommendation
1. Open app → Login
2. Navigate to Profile
3. Click "Recommend Health"
4. Select "Drink more water"
5. ✅ See "You earned 10 XP!"
6. ✅ See "You have 2 recommendations left"
7. Click "Done"
8. ✅ Profile shows updated XP

### Test Flow 2: View History
1. From Profile, click "View History"
2. ✅ See list of sent recommendations
3. ✅ Each shows: recipient, message, time

### Test Flow 3: Reach Limit
1. Send 3 recommendations
2. Try to send 4th
3. ✅ See "No recommendations left today!"

## 🔧 Customization

### Change XP Amount
File: `RecommendationOptionsActivity.java` (line 62)
```java
int earnedXP = 10; // Change to 20, 50, etc.
```

### Change Max Recommendations
File: `RecommendationManager.java` (line 14)
```java
.setValue(3); // Change to 5, 10, etc.
```

### Add More Options
1. Add button in `activity_recommendation_options.xml`
2. Add click handler in `RecommendationOptionsActivity.java`

## 🐛 Troubleshooting

### Issue: "Must be in a group"
**Solution:** Assign user to a group in Firebase
```java
TestDataSeeder.seedTestGroup(userId);
```

### Issue: "No recommendations left"
**Solution:** Reset in Firebase Console
```
users/{userId}/recommendationsLeft = 3
```

### Issue: XP not updating
**Solution:** Check Firebase rules allow write access

### Issue: History is empty
**Solution:** Send at least one recommendation first

## 📞 Need Help?

### Quick Reference
- Commands: `QUICK_REFERENCE.md`
- Architecture: `SYSTEM_DIAGRAM.txt`
- Checklist: `DEPLOYMENT_CHECKLIST.md`

### Firebase Paths
```
XP Points:           users/{userId}/totalPoints
Recommendations:     users/{userId}/recommendationsLeft
User's Group:        users/{userId}/groupId
Sent Messages:       recommendations/{recId}
```

### Common Commands
```java
// Initialize user
RecommendationManager.initializeUserRecommendations(userId);

// Reset recommendations
RecommendationManager.resetDailyRecommendations();

// Create test group
TestDataSeeder.seedTestGroup(userId);

// Navigate to profile
startActivity(new Intent(this, ProfileActivity.class));
```

## ✨ Success Checklist

- [ ] App builds without errors
- [ ] Can navigate to Profile
- [ ] Can send recommendations
- [ ] XP is awarded (10 per recommendation)
- [ ] Limit of 3 is enforced
- [ ] History shows sent recommendations
- [ ] Firebase data is correct

## 🎊 You're All Set!

The system is ready to use. Follow the Quick Start steps above to test it out.

For detailed information, check the other documentation files.

**Happy coding! 🚀**

---

**Created:** Health Recommendation System v1.0
**Features:** XP Points, Recommendation Limits, Group Messaging, History Tracking
**Status:** ✅ Ready to Use
