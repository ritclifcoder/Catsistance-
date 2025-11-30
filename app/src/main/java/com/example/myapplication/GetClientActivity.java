package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.WithingAPI.HealthData;
import com.example.myapplication.WithingAPI.WithingsApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class GetClientActivity extends AppCompatActivity {
    
    private static final String TAG = "GetClientActivity";
    private static final String BASE_URL = "https://wbsapi.withings.net/";
    
    private TextView bloodPressureText;
    private TextView stepsText;
    private TextView weightText;
    
    private WithingsApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_client);
        
        initViews();
        setupRetrofit();
        loadMockData(); // Using mock data for demo
    }
    
    private void initViews() {
        bloodPressureText = findViewById(R.id.bloodPressureText);
        stepsText = findViewById(R.id.stepsText);
        weightText = findViewById(R.id.weightText);
    }
    
    private void setupRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(WithingsApiService.class);
    }
    
    private void loadMockData() {
        // Mock data for demonstration
        HealthData mockData = new HealthData(
            new HealthData.BloodPressure(120, 80),
            8542,
            null,
            70.0
        );
        
        updateUI(mockData);
        Toast.makeText(this, "Mock data loaded successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void loadRealData() {
        // This would be used for real API calls
        String authToken = "Bearer YOUR_ACCESS_TOKEN"; // Replace with actual token
        
        // Example API call for blood pressure
        Call<com.example.myapplication.WithingAPI.WithingsResponse> call = apiService.getBloodPressure(
            authToken,
            "getmeas",
            "10,11" // Blood pressure measure types
        );
        
        call.enqueue(new Callback<com.example.myapplication.WithingAPI.WithingsResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.WithingAPI.WithingsResponse> call, Response<com.example.myapplication.WithingAPI.WithingsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body().toHealthData());
                    Toast.makeText(GetClientActivity.this, "Data loaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                    Toast.makeText(GetClientActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.myapplication.WithingAPI.WithingsResponse> call, Throwable t) {
                Log.e(TAG, "API call error: " + t.getMessage());
                Toast.makeText(GetClientActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(HealthData data) {
        if (data.getBloodPressure() != null) {
            bloodPressureText.setText(data.getBloodPressure().toString());
        }
        
        stepsText.setText(String.format("%,d", data.getStepsToday()));
        weightText.setText(String.format("%.1f kg", data.getWeight()));
    }
}