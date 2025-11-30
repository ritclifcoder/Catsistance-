package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.WithingAPI.WithingsClient;
import com.example.myapplication.WithingAPI.WithingsTokenManager;
import com.example.myapplication.WithingAPI.HealthData;

public class ProfileStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_statistics);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        fetchHealthData();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void fetchHealthData() {
        TextView stepsChart = findViewById(R.id.stepsChart);
        TextView bpChart = findViewById(R.id.bpChart);
        TextView weightChart = findViewById(R.id.weightChart);
        
        String accessToken = WithingsTokenManager.getInstance(this).getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            stepsChart.setText("⚠️ No Withings token");
            bpChart.setText("⚠️ No Withings token");
            weightChart.setText("⚠️ No Withings token");
            return;
        }
        
        WithingsClient client = WithingsClient.getInstance();
        
        client.getSteps(accessToken, "", "", new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                runOnUiThread(() -> {
                    int steps = data.getStepsToday();
                    int percent = Math.min(100, (steps * 100) / 10000);
                    String bar = makeBar(percent);
                    stepsChart.setText("Steps: " + steps + "\n\n" + bar + "\n\nGoal: 10,000 (" + percent + "%)");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> stepsChart.setText("❌ " + error));
            }
        });
        
        client.getBloodPressure(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                runOnUiThread(() -> {
                    if (data.getBloodPressure() != null) {
                        int sys = data.getBloodPressure().getSystolic();
                        int dia = data.getBloodPressure().getDiastolic();
                        String sysBar = makeBar(Math.min(100, (sys * 100) / 180));
                        String diaBar = makeBar(Math.min(100, (dia * 100) / 120));
                        bpChart.setText("BP: " + sys + "/" + dia + " mmHg\n\nSys: " + sysBar + "\nDia: " + diaBar + "\n\nStatus: Normal");
                    } else {
                        bpChart.setText("⚠️ No BP data");
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> bpChart.setText("❌ " + error));
            }
        });
        
        client.getWeight(accessToken, new WithingsClient.HealthDataCallback() {
            @Override
            public void onSuccess(HealthData data) {
                runOnUiThread(() -> {
                    double weight = data.getWeight();
                    double goal = 70.0;
                    int percent = (int)Math.min(100, (weight * 100) / (goal * 1.2));
                    String bar = makeBar(percent);
                    weightChart.setText("Weight: " + String.format("%.1f kg", weight) + "\n\n" + bar + "\n\nGoal: 70.0 kg");
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> weightChart.setText("❌ " + error));
            }
        });
    }
    
    private String makeBar(int percent) {
        int filled = percent / 5;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }
}
