# Quick Reference - Health Recommendation System

## 🎯 Quick Access

### Open Profile
```
Dashboard → Menu → Profile
```

### Send Recommendation
```
Profile → Recommend Health → Select Option
```

### View History
```
Profile → View History
OR
Dashboard → Menu → Recommendation History
```

## 📊 Key Values

| Item | Value | Location |
|------|-------|----------|
| XP per recommendation | 10 | `RecommendationOptionsActivity.java:62` |
| Max recommendations | 3 | `RecommendationManager.java:14` |
| Recommendation options | 4 | `activity_recommendation_options.xml` |

## 🗄️ Firebase Paths

```
users/{userId}/totalPoints              ← XP points
users/{userId}/recommendationsLeft      ← Remaining (0-3)
users/{userId}/groupId                  ← User's group
recommendations/{recId}/from            ← Sender ID
recommendations/{recId}/to              ← Recipient ID
recommendations/{recId}/message         ← Health tip
recommendations/{recId}/timestamp       ← When sent
```

## 🔧 Common Tasks

### Reset User's Recommendations
```java
FirebaseDatabase.getInstance()
    .getReference("users").child(userId)
    .child("recommendationsLeft").setValue(3);
```

### Add XP Manually
```java
FirebaseDatabase.getInstance()
    .getReference("users").child(userId)
    .child("totalPoints").setValue(100);
```

### Create Test Group
```java
TestDataSeeder.seedTestGroup(userId);
```

### Check User's Group
```java
FirebaseDatabase.getInstance()
    .getReference("users").child(userId).child("groupId")
    .get().addOnSuccessListener(snapshot -> {
        String groupId = snapshot.getValue(String.class);
    });
```

## 🐛 Troubleshooting

### "No recommendations left"
- Check: `users/{userId}/recommendationsLeft` in Firebase
- Fix: Set value to 1, 2, or 3

### "Must be in a group"
- Check: `users/{userId}/groupId` exists
- Fix: Assign user to a group or use `TestDataSeeder.seedTestGroup(userId)`

### XP not updating
- Check: `users/{userId}/totalPoints` in Firebase
- Verify: Congratulations screen shows correct XP
- Check: Firebase rules allow write access

### Recommendations not sending
- Check: User has groupId
- Check: Group has members
- Check: recommendationsLeft > 0
- Check: Firebase rules allow write to recommendations node

## 📱 User Messages

| Scenario | Message |
|----------|---------|
| 3 left | "You have 3 recommendations left" |
| 2 left | "You have 2 recommendations left" |
| 1 left | "You have 1 recommendation left" |
| 0 left | "No recommendations left today" |
| Success | "You earned 10 XP!" |
| No group | "You must be in a group to send recommendations" |

## 🎨 UI Components

### ProfileActivity
- Title: "Profile"
- XP display
- Recommendations counter
- "Recommend Health" button
- "View History" button

### RecommendationOptionsActivity
- 4 emoji buttons with health tips
- Auto-sends to all group members
- Auto-navigates to congratulations

### CongratulationsActivity
- 🎉 emoji
- XP earned message
- Recommendations left message
- "Done" button

### RecommendationHistoryActivity
- RecyclerView list
- Shows: recipient, message, timestamp
- Empty state if no history

## 🚀 Testing Flow

1. Login/Signup
2. Navigate to Profile
3. Click "Recommend Health"
4. Select "Drink more water"
5. See "You earned 10 XP!"
6. See "You have 2 recommendations left"
7. Click "Done"
8. Check Profile shows updated XP
9. Click "View History"
10. See sent recommendation

## 📞 Support

Check these files for details:
- `IMPLEMENTATION_SUMMARY.md` - Full overview
- `INTEGRATION_GUIDE.md` - Setup instructions
- `RECOMMENDATION_SYSTEM_README.md` - Feature details
