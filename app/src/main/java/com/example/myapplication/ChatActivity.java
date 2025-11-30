package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.AmazonBedrockService.BedrockService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private TextView chatMessages;
    private Button sendButton;
    private BedrockService bedrockService;

  FirebaseDatabase firebaseDatabase;
  DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messageInput = findViewById(R.id.messageInput);
        chatMessages = findViewById(R.id.chatMessages);
        sendButton = findViewById(R.id.sendButton);
        
        bedrockService = new BedrockService();

        sendButton.setOnClickListener(v -> sendMessage());

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Already enabled
        }
        
        firebaseDatabase = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Users");
        
        appendMessage("🐱 Hello! I'm your Catsistance Health Assistant.\n\nI'm here to help you with:\n• Blood pressure and heart health\n• Fitness and exercise tips\n• Sleep and recovery advice\n• Nutrition and diet guidance\n• Stress management\n• General wellness questions\n\nWhat health question can I help you with today?");







    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        appendMessage("You: " + message);
        messageInput.setText("");
        sendButton.setEnabled(false);

        String healthPrompt = "You are a health assistant for Catsistance app. Answer health-related questions about fitness, nutrition, blood pressure, heart rate, sleep, and wellness. Keep responses helpful and encouraging. User question: " + message;
        
        bedrockService.sendMessage(healthPrompt, response -> {
            runOnUiThread(() -> {
                appendMessage("🐱 Health Assistant: " + response);
                sendButton.setEnabled(true);
            });
        });
    }

    private void appendMessage(String message) {
        if (message != null && message.length() > 50000) {
            message = message.substring(0, 50000) + "... (truncated)";
        }
        chatMessages.append(message + "\n\n");
    }
    
    private void testFirebase() {
        databaseReference.child("test").setValue("Firebase çalışıyor!")
            .addOnSuccessListener(aVoid -> appendMessage("✓ Firebase: Veri yazıldı!"))
            .addOnFailureListener(e -> appendMessage("✗ Firebase Hata: " + e.getMessage()));
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}