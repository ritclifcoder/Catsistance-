# Health Recommendation System

## Features Implemented

### 1. ProfileActivity
- Contains "Recommend Health" button
- Checks if user has recommendations left before proceeding
- Navigates to RecommendationOptionsActivity

### 2. RecommendationOptionsActivity
- 4 health recommendation options:
  - 💧 Drink more water
  - 🚶 Walk more
  - ❤️ Lower your blood pressure
  - 😌 Reduce stress levels
- Sends recommendations only to users in the same group
- Limits to 3 recommendations per user
- Awards 10 XP per recommendation sent
- Updates totalPoints in Firebase

### 3. CongratulationsActivity
- Shows XP earned (10 XP per recommendation)
- Displays remaining recommendations (2 left, 1 left, or 0 left)
- Provides feedback to user

### 4. RecommendationHistoryActivity
- Shows all recommendations sent by the user
- Displays: recipient name, message, timestamp
- Uses RecyclerView for list display

## Firebase Database Structure

```
users/
  {userId}/
    username: "John Doe"
    groupId: "group123"
    totalPoints: 150
    recommendationsLeft: 2

groups/
  {groupId}/
    name: "Health Warriors"
    members/
      {userId1}: true
      {userId2}: true

recommendations/
  {recommendationId}/
    from: "userId1"
    to: "userId2"
    message: "Drink more water"
    timestamp: 1234567890
```

## How to Use

1. **Initialize User Data**: Call `RecommendationManager.initializeUserRecommendations(userId)` when user signs up or logs in

2. **Open Profile**: Start ProfileActivity from your app
   ```java
   startActivity(new Intent(this, ProfileActivity.class));
   ```

3. **View History**: Start RecommendationHistoryActivity
   ```java
   startActivity(new Intent(this, RecommendationHistoryActivity.class));
   ```

## XP System
- Each recommendation sent = 10 XP
- XP is added to `users/{userId}/totalPoints`
- Can be used for leaderboards, achievements, etc.

## Limitations
- 3 recommendations per user (stored in `recommendationsLeft`)
- Can only send to users in the same group
- Recommendations are sent to ALL group members (except sender)

## Future Enhancements
- Daily reset of recommendationsLeft (use WorkManager)
- Push notifications when receiving recommendations
- Different XP values for different recommendation types
- Recommendation acceptance/rejection system
