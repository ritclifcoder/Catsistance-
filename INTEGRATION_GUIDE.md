# Integration Guide - Health Recommendation System

## Quick Start

### 1. Add Profile Menu Item to Navigation Drawer

Open `res/menu/drawer_menu.xml` (or create it) and add:

```xml
<item
    android:id="@+id/nav_profile"
    android:icon="@android:drawable/ic_menu_myplaces"
    android:title="Profile" />
```

### 2. Add Navigation Handler in DashboardActivity

In your `DashboardActivity.java`, add this to the navigation listener:

```java
} else if (item.getItemId() == R.id.nav_profile) {
    drawerLayout.closeDrawers();
    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
    return true;
}
```

### 3. Test the Flow

1. Run the app
2. Navigate to Profile from drawer
3. Click "Recommend Health" button
4. Select a health recommendation
5. See congratulations screen with XP earned
6. Check Firebase database for updated points

### 4. View Recommendation History

Add another menu item or button to launch:
```java
startActivity(new Intent(this, RecommendationHistoryActivity.class));
```

## Firebase Database Setup

The system automatically creates these nodes:
- `users/{userId}/totalPoints` - XP points
- `users/{userId}/recommendationsLeft` - Remaining recommendations (max 3)
- `recommendations/{recId}` - Sent recommendations

## Customization

### Change XP Amount
In `RecommendationOptionsActivity.java`, line 62:
```java
int earnedXP = 10; // Change this value
```

### Change Max Recommendations
In `RecommendationManager.java`, line 14:
```java
db.getReference("users").child(userId).child("recommendationsLeft").setValue(3); // Change 3 to desired number
```

### Add More Recommendation Options
In `activity_recommendation_options.xml`, add more buttons:
```xml
<Button
    android:id="@+id/btnNewOption"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="🏃 Exercise daily"
    android:textSize="16sp"
    android:layout_marginBottom="16dp" />
```

Then in `RecommendationOptionsActivity.java`:
```java
findViewById(R.id.btnNewOption).setOnClickListener(v -> sendRecommendation("Exercise daily"));
```

## Testing Checklist

- [ ] User can click "Recommend Health" button
- [ ] 4 recommendation options are displayed
- [ ] Recommendations are sent to group members only
- [ ] XP is added to totalPoints in Firebase
- [ ] Congratulations screen shows correct XP and remaining count
- [ ] After 3 recommendations, user sees "No recommendations left"
- [ ] Recommendation history shows sent messages
- [ ] New users start with 3 recommendations and 0 points
