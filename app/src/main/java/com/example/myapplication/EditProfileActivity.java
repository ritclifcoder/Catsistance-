package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {
    
    private static final int PICK_IMAGE = 1;
    private ImageView profileImage;
    private EditText nameInput, emailInput, ageInput;
    private Uri imageUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        ageInput = findViewById(R.id.ageInput);
        Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        Button saveButton = findViewById(R.id.saveButton);
        
        uploadPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });
        
        saveButton.setOnClickListener(v -> saveProfile());
        
        loadProfile();
    }
    
    private void loadProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/").getReference("Users").child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String age = snapshot.child("age").getValue(String.class);
                        String photoUri = snapshot.child("photoUri").getValue(String.class);
                        
                        if (name != null) nameInput.setText(name);
                        if (email != null) emailInput.setText(email);
                        if (age != null) ageInput.setText(age);
                        if (photoUri != null && !photoUri.startsWith("content://")) {
                            profileImage.setImageResource(R.drawable.catpuccino);
                        }
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    profileImage.setImageURI(imageUri);
                } catch (Exception e) {
                    profileImage.setImageURI(imageUri);
                }
            }
        }
    }
    
    private void saveProfile() {
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String age = ageInput.getText().toString();
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        
        db.getReference("Users").child(userId).child("name").setValue(name);
        db.getReference("Users").child(userId).child("email").setValue(email);
        db.getReference("Users").child(userId).child("age").setValue(age);
        
        if (imageUri != null && imageUri.toString().startsWith("content://")) {
            StorageReference storageRef = FirebaseStorage.getInstance("gs://fir-adapterrecyclerview-1f230.appspot.com").getReference("catsistance/" + userId + ".jpg");
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> 
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.getReference("Users").child(userId).child("photoUri").setValue(uri.toString());
                    Toast.makeText(this, "✅ Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
            ).addOnFailureListener(e -> {
                android.util.Log.e("EditProfile", "Storage upload failed: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "❌ Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            Toast.makeText(this, "✅ Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
