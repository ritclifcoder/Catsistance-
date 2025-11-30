# Deployment Checklist - Health Recommendation System

## ✅ Pre-Deployment Checklist

### 1. Build & Compile
- [ ] Clean project: `Build → Clean Project`
- [ ] Rebuild project: `Build → Rebuild Project`
- [ ] Fix any compilation errors
- [ ] Verify all imports are resolved

### 2. Firebase Setup
- [ ] Firebase Realtime Database is enabled
- [ ] Database rules allow read/write for authenticated users
- [ ] Test Firebase connection from app

### 3. Test User Setup
- [ ] Create test user account
- [ ] Verify user has `totalPoints` = 0
- [ ] Verify user has `recommendationsLeft` = 3
- [ ] Assign user to a test group

### 4. Group Setup
- [ ] Create test group in Firebase
- [ ] Add at least 2 users to the group
- [ ] Verify group structure:
  ```
  groups/
    {groupId}/
      name: "Test Group"
      members/
        {userId1}: true
        {userId2}: true
  ```

### 5. Navigation Testing
- [ ] Open app → Dashboard
- [ ] Open navigation drawer
- [ ] Verify "Profile" menu item exists
- [ ] Verify "Recommendation History" menu item exists
- [ ] Click "Profile" → ProfileActivity opens
- [ ] Click "Recommendation History" → RecommendationHistoryActivity opens

### 6. Profile Screen Testing
- [ ] ProfileActivity displays correctly
- [ ] "Total XP: 0" is shown
- [ ] "Recommendations Left: 3/3" is shown
- [ ] "Recommend Health" button is visible
- [ ] "View History" button is visible

### 7. Recommendation Flow Testing
- [ ] Click "Recommend Health"
- [ ] RecommendationOptionsActivity opens
- [ ] All 4 buttons are visible:
  - [ ] 💧 Drink more water
  - [ ] 🚶 Walk more
  - [ ] ❤️ Lower your blood pressure
  - [ ] 😌 Reduce stress levels
- [ ] Click any option
- [ ] CongratulationsActivity opens
- [ ] "You earned 10 XP!" is displayed
- [ ] "You have 2 recommendations left" is displayed
- [ ] Click "Done" → Returns to previous screen

### 8. Firebase Data Verification
After sending 1 recommendation:
- [ ] Check `users/{userId}/totalPoints` = 10
- [ ] Check `users/{userId}/recommendationsLeft` = 2
- [ ] Check `recommendations/{recId}` exists with:
  - [ ] from: {userId}
  - [ ] to: {recipientId}
  - [ ] message: "Drink more water" (or selected option)
  - [ ] timestamp: {current timestamp}

### 9. Limit Testing
- [ ] Send 2nd recommendation → "You have 1 recommendation left"
- [ ] Send 3rd recommendation → "No recommendations left today"
- [ ] Try to send 4th → "No recommendations left today!" toast
- [ ] Verify recommendationsLeft = 0 in Firebase

### 10. History Testing
- [ ] Click "View History" from Profile
- [ ] Verify sent recommendations are listed
- [ ] Verify each item shows:
  - [ ] "To: {recipient name}"
  - [ ] Message text
  - [ ] Timestamp (formatted)

### 11. XP Display Testing
- [ ] Return to ProfileActivity
- [ ] Verify "Total XP: 30" (if sent 3 recommendations)
- [ ] Verify "Recommendations Left: 0/3"

### 12. Edge Cases
- [ ] User not in a group → "Must be in a group" message
- [ ] No recommendations left → Button shows toast
- [ ] Empty history → RecyclerView shows empty state
- [ ] Multiple users in group → All receive recommendation

## 🔧 Optional Enhancements

### Daily Reset (Future)
- [ ] Implement WorkManager for daily reset
- [ ] Schedule reset at midnight
- [ ] Test reset functionality

### Push Notifications (Future)
- [ ] Set up FCM for notifications
- [ ] Send notification when recommendation received
- [ ] Test notification delivery

### Leaderboard Integration (Future)
- [ ] Create leaderboard activity
- [ ] Sort users by totalPoints
- [ ] Display top 10 users

## 📱 Device Testing

### Test on Multiple Devices
- [ ] Phone (Android 8+)
- [ ] Tablet
- [ ] Different screen sizes
- [ ] Different Android versions

### Test Scenarios
- [ ] Fresh install
- [ ] Existing user
- [ ] User with 0 recommendations left
- [ ] User with high XP
- [ ] User not in a group

## 🐛 Known Issues to Check

- [ ] Firebase connection timeout handling
- [ ] Null pointer exceptions
- [ ] Empty group handling
- [ ] Network error handling
- [ ] UI thread blocking

## 📊 Performance Checks

- [ ] App launches quickly
- [ ] Firebase queries are fast
- [ ] No ANR (Application Not Responding)
- [ ] Smooth scrolling in RecyclerView
- [ ] No memory leaks

## 🔒 Security Checks

- [ ] Firebase rules prevent unauthorized access
- [ ] User can only send recommendations if authenticated
- [ ] User can only view their own history
- [ ] XP cannot be manipulated by client

## 📝 Documentation

- [ ] README files are up to date
- [ ] Code comments are clear
- [ ] Firebase structure is documented
- [ ] API usage is documented

## 🚀 Ready for Production

Once all items are checked:
- [ ] Create release build
- [ ] Test release build on device
- [ ] Upload to Play Store (if applicable)
- [ ] Monitor Firebase for errors
- [ ] Monitor user feedback

## 📞 Support Resources

- `IMPLEMENTATION_SUMMARY.md` - Full overview
- `INTEGRATION_GUIDE.md` - Setup instructions
- `QUICK_REFERENCE.md` - Quick commands
- `SYSTEM_DIAGRAM.txt` - Architecture diagram
- `RECOMMENDATION_SYSTEM_README.md` - Feature details

## ✨ Success Criteria

The system is ready when:
1. ✅ User can send health recommendations
2. ✅ XP is awarded correctly (10 per recommendation)
3. ✅ Limit of 3 recommendations is enforced
4. ✅ Only group members receive recommendations
5. ✅ History shows all sent recommendations
6. ✅ UI is responsive and user-friendly
7. ✅ Firebase data is structured correctly
8. ✅ No crashes or errors occur

---

**Date Completed:** _______________
**Tested By:** _______________
**Status:** ⬜ Ready for Production | ⬜ Needs Work
