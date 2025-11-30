package com.example.myapplication.utils;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

public class MockPhotoUploader {
    
    public static void addMockPhotosToAllUsers() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        
        db.getReference("Users").get().addOnSuccessListener(snapshot -> {
            int index = 1;
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                String userId = userSnapshot.getKey();
                String username = userSnapshot.child("username").exists() ? 
                    userSnapshot.child("username").getValue(String.class) : "User";
                
                // Use different mock photo services
                String photoUrl;
                if (index % 3 == 0) {
                    photoUrl = "https://i.pravatar.cc/200?img=" + index;
                } else if (index % 3 == 1) {
                    photoUrl = "https://ui-avatars.com/api/?name=" + username + "&size=200&background=random";
                } else {
                    photoUrl = "https://picsum.photos/200?random=" + index;
                }
                
                db.getReference("Users").child(userId).child("photoUri").setValue(photoUrl);
                android.util.Log.d("MockPhotoUploader", "Added photo for " + username + ": " + photoUrl);
                index++;
            }
        });
    }
    
    public static void addMockPhotoToCurrentUser(String userId, String username) {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        String photoUrl = "https://ui-avatars.com/api/?name=" + username + "&size=200&background=4CAF50&color=fff";
        db.getReference("Users").child(userId).child("photoUri").setValue(photoUrl);
    }
}
