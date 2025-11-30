package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private NotificationAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notifications");
        
        recyclerView = findViewById(R.id.notificationRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadNotifications();
    }
    
    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        android.util.Log.d("ReminderActivity", "Loading notifications for user: " + userId);
        
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
            .getReference("notifications")
            .child(userId);
        
        progressBar.setVisibility(View.VISIBLE);
        
        notifRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NotificationItem> notifications = new ArrayList<>();
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    NotificationItem item = data.getValue(NotificationItem.class);
                    if (item != null) {
                        notifications.add(item);
                    }
                }
                
                Collections.reverse(notifications);
                
                progressBar.setVisibility(View.GONE);
                
                if (notifications.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new NotificationAdapter(notifications);
                    recyclerView.setAdapter(adapter);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("ReminderActivity", "Error: " + error.getMessage());
                android.util.Log.e("ReminderActivity", "Code: " + error.getCode());
                android.util.Log.e("ReminderActivity", "Details: " + error.getDetails());
                
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Error: " + error.getMessage());
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    public static class NotificationItem {
        public String title;
        public String message;
        public long timestamp;
        
        public NotificationItem() {}
        
        public NotificationItem(String title, String message, long timestamp) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<NotificationItem> items;
        
        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.title.setText(item.title);
            holder.message.setText(item.message);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            holder.time.setText(sdf.format(new Date(item.timestamp)));
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, message, time;
            
            ViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.notificationTitle);
                message = view.findViewById(R.id.notificationMessage);
                time = view.findViewById(R.id.notificationTime);
            }
        }
    }
}
