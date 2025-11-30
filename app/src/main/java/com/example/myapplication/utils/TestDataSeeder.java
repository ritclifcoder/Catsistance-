package com.example.myapplication.utils;

import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class TestDataSeeder {
    
    public static void seedTestGroup(String userId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String groupId = "testGroup123";
        
        // Create test group
        Map<String, Object> group = new HashMap<>();
        group.put("name", "Test Health Group");
        group.put("members/" + userId, true);
        group.put("members/testUser1", true);
        group.put("members/testUser2", true);
        
        db.getReference("groups").child(groupId).setValue(group);
        
        // Assign user to group
        db.getReference("users").child(userId).child("groupId").setValue(groupId);
        
        // Create test users
        createTestUser(db, "testUser1", "Alice", "alice@test.com");
        createTestUser(db, "testUser2", "Bob", "bob@test.com");
    }
    
    private static void createTestUser(FirebaseDatabase db, String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", name);
        user.put("email", email);
        user.put("totalPoints", 0);
        user.put("recommendationsLeft", 3);
        user.put("groupId", "testGroup123");
        
        db.getReference("users").child(userId).setValue(user);
    }
    
    public static void resetRecommendations(String userId) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId).child("recommendationsLeft").setValue(3);
    }
}
