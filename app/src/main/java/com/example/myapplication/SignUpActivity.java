package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.models.HealthStats;
import com.example.myapplication.models.User;
import com.example.myapplication.models.Vitals;
import com.example.myapplication.utils.RecommendationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    
    private EditText nameInput, emailInput, passwordInput;
    private Button signUpButton;
    private TextView loginText;

    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/")
                .getReference("Users");
        
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signUpButton = findViewById(R.id.signUpButton);
        loginText = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progressBar);

        
        signUpButton.setOnClickListener(v -> signUpUser());
        loginText.setOnClickListener(v -> finish());
    }
    
    private void signUpUser() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        signUpButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                User newUser = new User(userId, name, email, 0, "Silver", 
                        new HealthStats(0, 0), new Vitals(0, 0, 0));
                
                usersRef.child(userId).setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        RecommendationManager.initializeUserRecommendations(userId);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        signUpButton.setEnabled(true);
                    });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                signUpButton.setEnabled(true);
            });
    }
}
