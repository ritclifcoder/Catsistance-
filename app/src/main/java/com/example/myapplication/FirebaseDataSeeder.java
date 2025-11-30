package com.example.myapplication;

import com.example.myapplication.models.HealthStats;
import com.example.myapplication.models.User;
import com.example.myapplication.models.Vitals;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataSeeder {
    
    public static void seedData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/")
                .getReference("Users");
        
        // Silver Group
        createUser(usersRef, "Alice Johnson", "alice@example.com", 950, "Silver", 12500, 8.0, 118, 78, 68);
        createUser(usersRef, "Bob Smith", "bob@example.com", 890, "Silver", 11200, 7.5, 122, 80, 72);
        createUser(usersRef, "Carol White", "carol@example.com", 850, "Silver", 10800, 7.0, 120, 82, 75);
        
        // Gold Group
        createUser(usersRef, "David Brown", "david@example.com", 920, "Gold", 13000, 7.5, 125, 85, 70);
        createUser(usersRef, "Emma Davis", "emma@example.com", 880, "Gold", 10500, 6.5, 128, 86, 78);
        createUser(usersRef, "Frank Miller", "frank@example.com", 840, "Gold", 9800, 7.0, 120, 80, 74);
        
        // Master Group
        createUser(usersRef, "Grace Lee", "grace@example.com", 940, "Master", 12800, 8.0, 115, 75, 65);
        createUser(usersRef, "Henry Wilson", "henry@example.com", 870, "Master", 10200, 7.0, 122, 82, 73);
        createUser(usersRef, "Ivy Chen", "ivy@example.com", 830, "Master", 9500, 6.5, 126, 84, 76);
        
        // Elite Group
        createUser(usersRef, "Jack Taylor", "jack@example.com", 910, "Elite", 11800, 7.5, 119, 79, 69);
        createUser(usersRef, "Kate Anderson", "kate@example.com", 860, "Elite", 10000, 7.0, 123, 81, 71);
        createUser(usersRef, "Leo Martinez", "leo@example.com", 820, "Elite", 9200, 6.5, 127, 85, 77);
    }
    
    private static void createUser(DatabaseReference usersRef, String name, String email, int points, 
                                   String group, int steps, double sleep, int sys, int dia, int hr) {
        String userId = usersRef.push().getKey();
        User user = new User(userId, name, email, points, group, 
                new HealthStats(steps, sleep), new Vitals(sys, dia, hr));
        usersRef.child(userId).setValue(user);
    }
}
