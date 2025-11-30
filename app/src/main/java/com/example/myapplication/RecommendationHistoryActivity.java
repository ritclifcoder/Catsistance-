package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class RecommendationHistoryActivity extends AppCompatActivity {

    private RecyclerView sentRecyclerView;
    private RecyclerView receivedRecyclerView;
    private RecommendationAdapter sentAdapter;
    private RecommendationAdapter receivedAdapter;
    private List<RecommendationItem> sentRecommendations = new ArrayList<>();
    private List<RecommendationItem> receivedRecommendations = new ArrayList<>();
    private android.widget.TextView sentEmptyText;
    private android.widget.TextView receivedEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_history);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sentRecyclerView = findViewById(R.id.sentRecyclerView);
        receivedRecyclerView = findViewById(R.id.receivedRecyclerView);
        sentEmptyText = findViewById(R.id.sentEmptyText);
        receivedEmptyText = findViewById(R.id.receivedEmptyText);
        
        sentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receivedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        sentAdapter = new RecommendationAdapter(sentRecommendations);
        receivedAdapter = new RecommendationAdapter(receivedRecommendations);
        
        sentRecyclerView.setAdapter(sentAdapter);
        receivedRecyclerView.setAdapter(receivedAdapter);

        loadSentRecommendations();
        loadReceivedRecommendations();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadSentRecommendations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        
        db.getReference("Users").child(userId).child("recommendations")
            .get().addOnSuccessListener(snapshot -> {
                sentRecommendations.clear();
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    sentAdapter.notifyDataSetChanged();
                    sentEmptyText.setVisibility(android.view.View.VISIBLE);
                    sentRecyclerView.setVisibility(android.view.View.GONE);
                    return;
                }
                sentEmptyText.setVisibility(android.view.View.GONE);
                sentRecyclerView.setVisibility(android.view.View.VISIBLE);
                
                for (DataSnapshot recSnapshot : snapshot.getChildren()) {
                    String to = recSnapshot.child("to").getValue(String.class);
                    String message = recSnapshot.child("message").getValue(String.class);
                    Long timestampLong = recSnapshot.child("timestamp").getValue(Long.class);
                    long timestamp = timestampLong != null ? timestampLong : 0;
                    
                    if (to != null) {
                        db.getReference("Users").child(to)
                            .get().addOnSuccessListener(userSnapshot -> {
                                String toUsername = "Unknown";
                                if (userSnapshot.exists()) {
                                    if (userSnapshot.child("username").exists()) {
                                        toUsername = userSnapshot.child("username").getValue(String.class);
                                    } else if (userSnapshot.child("name").exists()) {
                                        toUsername = userSnapshot.child("name").getValue(String.class);
                                    }
                                }
                                sentRecommendations.add(new RecommendationItem(toUsername, message, timestamp));
                                sentAdapter.notifyDataSetChanged();
                            });
                    }
                }
            });
    }
    
    private void loadReceivedRecommendations() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/");
        
        db.getReference("Users").child(userId).child("receivedRecommendations")
            .get().addOnSuccessListener(snapshot -> {
                receivedRecommendations.clear();
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    receivedAdapter.notifyDataSetChanged();
                    receivedEmptyText.setVisibility(android.view.View.VISIBLE);
                    receivedRecyclerView.setVisibility(android.view.View.GONE);
                    return;
                }
                receivedEmptyText.setVisibility(android.view.View.GONE);
                receivedRecyclerView.setVisibility(android.view.View.VISIBLE);
                
                for (DataSnapshot recSnapshot : snapshot.getChildren()) {
                    String from = recSnapshot.child("from").getValue(String.class);
                    String message = recSnapshot.child("message").getValue(String.class);
                    Long timestampLong = recSnapshot.child("timestamp").getValue(Long.class);
                    long timestamp = timestampLong != null ? timestampLong : 0;
                    
                    if (from != null) {
                        db.getReference("Users").child(from)
                            .get().addOnSuccessListener(userSnapshot -> {
                                String fromUsername = "Unknown";
                                if (userSnapshot.exists()) {
                                    if (userSnapshot.child("username").exists()) {
                                        fromUsername = userSnapshot.child("username").getValue(String.class);
                                    } else if (userSnapshot.child("name").exists()) {
                                        fromUsername = userSnapshot.child("name").getValue(String.class);
                                    }
                                }
                                receivedRecommendations.add(new RecommendationItem(fromUsername, message, timestamp));
                                receivedAdapter.notifyDataSetChanged();
                            });
                    }
                }
            });
    }

    static class RecommendationItem {
        String toUsername;
        String message;
        long timestamp;

        RecommendationItem(String toUsername, String message, long timestamp) {
            this.toUsername = toUsername;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    static class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
        private List<RecommendationItem> items;

        RecommendationAdapter(List<RecommendationItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RecommendationItem item = items.get(position);
            holder.toText.setText(item.toUsername);
            
            String emoji = "💪";
            if (item.message.contains("water")) emoji = "💧";
            else if (item.message.contains("Walk")) emoji = "🚶";
            else if (item.message.contains("blood pressure")) emoji = "❤️";
            else if (item.message.contains("stress")) emoji = "😌";
            
            holder.messageText.setText(emoji + " " + item.message);
            holder.timeText.setText(new java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm")
                .format(new java.util.Date(item.timestamp)));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView toText, messageText, timeText;

            ViewHolder(android.view.View view) {
                super(view);
                toText = view.findViewById(R.id.toText);
                messageText = view.findViewById(R.id.messageText);
                timeText = view.findViewById(R.id.timeText);
            }
        }
    }
}
