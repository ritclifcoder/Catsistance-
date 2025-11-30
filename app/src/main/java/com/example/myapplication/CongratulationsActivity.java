package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.utils.TTSHelperREST;

public class CongratulationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulations);

        int xpEarned = getIntent().getIntExtra("xpEarned", 0);
        int recommendationsLeft = getIntent().getIntExtra("recommendationsLeft", 0);

        TextView xpText = findViewById(R.id.xpEarnedText);
        TextView leftText = findViewById(R.id.recommendationsLeftText);

        xpText.setText("You earned " + xpEarned + " XP!");
        
        if (recommendationsLeft > 0) {
            leftText.setText("You have " + recommendationsLeft + " recommendation" + 
                (recommendationsLeft == 1 ? "" : "s") + " left");
        } else {
            leftText.setText("No recommendations left today");
        }

        Button doneBtn = findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(v -> finish());

        TTSHelperREST.speakText(this, "Merhaba, nasılsın?", true, true);

    }
}
