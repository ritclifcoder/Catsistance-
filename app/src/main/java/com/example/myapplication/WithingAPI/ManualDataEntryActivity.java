package com.example.myapplication.WithingAPI;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class ManualDataEntryActivity extends AppCompatActivity {
    
    private EditText sleepHoursInput, stepsInput, systolicInput, diastolicInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        
        android.widget.TextView title = new android.widget.TextView(this);
        title.setText("Enter Health Data");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);
        
        sleepHoursInput = new EditText(this);
        sleepHoursInput.setHint("Sleep Hours (e.g., 7.5)");
        sleepHoursInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(sleepHoursInput);
        
        stepsInput = new EditText(this);
        stepsInput.setHint("Steps (e.g., 8000)");
        stepsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(stepsInput);
        
        systolicInput = new EditText(this);
        systolicInput.setHint("Systolic BP (e.g., 120)");
        systolicInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(systolicInput);
        
        diastolicInput = new EditText(this);
        diastolicInput.setHint("Diastolic BP (e.g., 80)");
        diastolicInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(diastolicInput);
        
        Button saveButton = new Button(this);
        saveButton.setText("Save to Withings");
        saveButton.setOnClickListener(v -> saveData());
        layout.addView(saveButton);
        
        setContentView(layout);
    }
    
    private void saveData() {
        String sleep = sleepHoursInput.getText().toString();
        String steps = stepsInput.getText().toString();
        String systolic = systolicInput.getText().toString();
        String diastolic = diastolicInput.getText().toString();
        
        if (sleep.isEmpty() || steps.isEmpty() || systolic.isEmpty() || diastolic.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.content.SharedPreferences prefs = getSharedPreferences("HealthData", MODE_PRIVATE);
        prefs.edit()
            .putString("sleep", sleep)
            .putString("steps", steps)
            .putString("systolic", systolic)
            .putString("diastolic", diastolic)
            .apply();
        
        Toast.makeText(this, "Health data saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
