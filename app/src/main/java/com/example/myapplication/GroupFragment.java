package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.PointCalculator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupFragment extends Fragment {

    private RecyclerView recyclerView;
    private TabLayout groupTabLayout;
    private ProgressBar progressBar;
    
    private final int[] tabColors = {
        Color.parseColor("#C0C0C0"),  // Silver - Bright Silver
        Color.parseColor("#FFD700"),  // Gold - Yellow
        Color.parseColor("#FF69B4"),  // Tigers - Pink
        Color.parseColor("#9C27B0")   // Lions - Purple
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        
        recyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progressBar);
        
        groupTabLayout = view.findViewById(R.id.groupTabLayout);
        groupTabLayout.setVisibility(View.VISIBLE);
        groupTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        groupTabLayout.setTabMode(TabLayout.MODE_FIXED);
        
        groupTabLayout.addTab(groupTabLayout.newTab().setText("Silver Cats").setIcon(R.drawable.catcat));
        groupTabLayout.addTab(groupTabLayout.newTab().setText("Gold Bobcats").setIcon(R.drawable.bobdylancat));
        groupTabLayout.addTab(groupTabLayout.newTab().setText("Master Tigers").setIcon(R.drawable.kaplankaplan));
        groupTabLayout.addTab(groupTabLayout.newTab().setText("Elite Lions").setIcon(R.drawable.aslanaslan));
        
        applyTabIconTints();
        
        loadGroupLeaderboard(0);
        
        groupTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyTabIconTints();
                loadGroupLeaderboard(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                applyTabIconTints();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        return view;
    }
    
    private void applyTabIconTints() {
        for (int i = 0; i < groupTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = groupTabLayout.getTabAt(i);
            if (tab != null && tab.getIcon() != null) {
                tab.getIcon().setTint(tabColors[i]);
            }
        }
    }
    
    private static boolean isFemaleNameLikely(String firstName) {
        String[] femaleNames = {"alice", "emma", "sophia", "olivia", "ava", "isabella", "mia", "charlotte", "amelia", "harper", "evelyn", "abigail", "emily", "elizabeth", "sofia", "avery", "ella", "scarlett", "grace", "chloe", "victoria", "riley", "aria", "lily", "aubrey", "zoey", "penelope", "lillian", "addison", "layla", "natalie", "camila", "hannah", "brooklyn", "zoe", "nora", "leah", "savannah", "audrey", "claire", "eleanor", "skylar", "ellie", "samantha", "stella", "carol", "sarah", "jessica", "jennifer", "linda", "barbara", "susan", "karen", "nancy", "betty", "margaret", "sandra", "ashley", "dorothy", "kimberly", "donna", "michelle", "carol", "amanda", "melissa", "deborah", "stephanie", "rebecca", "laura", "sharon", "cynthia", "kathleen", "helen", "amy", "anna", "shirley", "angela", "ruth", "brenda", "pamela", "nicole", "katherine", "virginia", "catherine", "christine", "janet", "debra", "rachel", "carolyn", "emma", "maria", "heather", "diane", "julie", "joyce", "evelyn", "joan", "victoria", "kelly", "christina", "lauren", "frances"};
        for (String name : femaleNames) {
            if (firstName.equals(name)) return true;
        }
        return firstName.endsWith("a") || firstName.endsWith("e") || firstName.endsWith("ie") || firstName.endsWith("y");
    }
    
    private void loadGroupLeaderboard(int groupIndex) {
        String[] groups = {"Silver", "Gold", "Master", "Elite"};
        String selectedGroup = groups[groupIndex];
        
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/")
                .getReference("Users");

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);


        android.util.Log.d("GroupFragment", "Loading group: " + selectedGroup);
        
        usersRef.orderByChild("group").equalTo(selectedGroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("GroupFragment", "✅ Data received, count: " + snapshot.getChildrenCount());
                
                List<LeaderboardUser> users = new ArrayList<>();
                Set<String> addedNames = new HashSet<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !addedNames.contains(user.getName())) {
                        int displayPoints = userSnapshot.child("points").exists() ? 
                            userSnapshot.child("points").getValue(Integer.class) : 
                            PointCalculator.calculateTotalPoints(user.getHealthStats(), user.getVitals());
                        
                        String stats = "Steps: " + user.getHealthStats().getSteps() + " | Sleep: " + user.getHealthStats().getSleepHours() + "h";
                        String vitals = "BP: " + user.getVitals().getSystolic() + "/" + user.getVitals().getDiastolic() + " | HR: " + user.getVitals().getHeartRate() + " bpm";
                        LeaderboardUser leaderboardUser = new LeaderboardUser(userSnapshot.getKey(), user.getName(), displayPoints, stats, vitals);
                        String photoUri = userSnapshot.child("photoUri").exists() ? userSnapshot.child("photoUri").getValue(String.class) : null;
                        if (photoUri == null || photoUri.contains("google.com/url") || photoUri.startsWith("content://")) {
                            String firstName = user.getName().split(" ")[0].toLowerCase();
                            boolean isFemale = isFemaleNameLikely(firstName);
                            int baseId = isFemale ? 1 : 36;
                            int avatarId = baseId + (Math.abs(user.getName().hashCode() % 35));
                            photoUri = "https://i.pravatar.cc/200?img=" + avatarId;
                        }
                        leaderboardUser.photoUri = photoUri;
                        android.util.Log.d("GroupFragment", "User: " + user.getName() + ", photoUri: " + leaderboardUser.photoUri);
                        users.add(leaderboardUser);
                        addedNames.add(user.getName());
                    }
                }
                // Puanlarına göre yüksekten düşüğe sırala
                Collections.sort(users, new Comparator<LeaderboardUser>() {
                    @Override
                    public int compare(LeaderboardUser u1, LeaderboardUser u2) {
                        return Integer.compare(u2.points, u1.points);
                    }
                });

                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(new LeaderboardAdapter(users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("GroupFragment", "❌ Firebase error: " + error.getMessage());
                android.util.Log.e("GroupFragment", "Error code: " + error.getCode());
                android.util.Log.e("GroupFragment", "Error details: " + error.getDetails());
                
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
    

    
    static class LeaderboardUser {
        String userId;
        String name;
        int points;
        String stats;
        String vitals;
        String photoUri;
        
        LeaderboardUser(String userId, String name, int points, String stats, String vitals) {
            this.userId = userId;
            this.name = name;
            this.points = points;
            this.stats = stats;
            this.vitals = vitals;
        }
    }
    
    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<LeaderboardUser> users;
        
        LeaderboardAdapter(List<LeaderboardUser> users) {
            this.users = users;
        }
        
        private android.graphics.Bitmap getCircularBitmap(android.graphics.Bitmap bitmap) {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            android.graphics.Bitmap output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(output);
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
            paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f, paint);
            return output;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardUser user = users.get(position);
            
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), UsersActivity.class);
                intent.putExtra(UsersActivity.EXTRA_USER_ID, user.userId);
                intent.putExtra(UsersActivity.EXTRA_USER_NAME, user.name);
                intent.putExtra(UsersActivity.EXTRA_USER_POINTS, user.points);
                intent.putExtra(UsersActivity.EXTRA_USER_STATS, user.stats);
                intent.putExtra(UsersActivity.EXTRA_USER_VITALS, user.vitals);
                intent.putExtra(UsersActivity.EXTRA_USER_PHOTO_URI, user.photoUri);
                v.getContext().startActivity(intent);
            });
            holder.rankBadge.setText(String.valueOf(position + 1));
            holder.userName.setText(user.name);
            holder.healthPoints.setText(String.valueOf(user.points));
            
            // Load profile photo
            android.util.Log.d("GroupFragment", "Loading photo for " + user.name + ": " + user.photoUri);
            if (user.photoUri != null && !user.photoUri.isEmpty()) {
                if (user.photoUri.startsWith("content://")) {
                    // Local file - use ContentResolver
                    try {
                        android.net.Uri uri = android.net.Uri.parse(user.photoUri);
                        android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(holder.itemView.getContext().getContentResolver(), uri);
                        if (bitmap != null) {
                            holder.profilePhoto.setImageBitmap(getCircularBitmap(bitmap));
                        } else {
                            holder.profilePhoto.setImageResource(R.drawable.catpuccino);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("GroupFragment", "❌ Error loading local file: " + e.getMessage());
                        holder.profilePhoto.setImageResource(R.drawable.catpuccino);
                    }
                } else {
                    // Internet URL - download in background
                    new Thread(() -> {
                        try {
                            java.net.URL url = new java.net.URL(user.photoUri);
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            holder.itemView.post(() -> {
                                if (bitmap != null) {
                                    holder.profilePhoto.setImageBitmap(getCircularBitmap(bitmap));
                                } else {
                                    holder.profilePhoto.setImageResource(R.drawable.catpuccino);
                                }
                            });
                        } catch (Exception e) {
                            android.util.Log.e("GroupFragment", "❌ Error loading URL: " + e.getMessage());
                            holder.itemView.post(() -> holder.profilePhoto.setImageResource(R.drawable.catpuccino));
                        }
                    }).start();
                }
            } else {
                holder.profilePhoto.setImageResource(R.drawable.catpuccino);
            }
            
            // Calculate crypto equivalent (points / 100000)
            double cryptoValue = user.points / 100000.0;
            holder.cryptoEquivalent.setText(String.format("%.5f ETH", cryptoValue));
            
            // Parse stats
            String[] statsParts = user.stats.split("\\|");
            if (statsParts.length >= 2) {
                holder.stepsText.setText(statsParts[0].replace("Steps:", "").trim());
                holder.sleepText.setText(statsParts[1].replace("Sleep:", "").trim());
            }
            
            // Parse vitals
            String[] vitalsParts = user.vitals.split("\\|");
            if (vitalsParts.length >= 2) {
                holder.bpText.setText(vitalsParts[0].replace("BP:", "").trim());
                holder.hrText.setText(vitalsParts[1].replace("HR:", "").trim());
            }
            
            if (position == 0) {
                holder.rankBadge.setBackgroundColor(Color.parseColor("#FFD700"));
                holder.groupBadge.setText("🥇");
            } else if (position == 1) {
                holder.rankBadge.setBackgroundColor(Color.parseColor("#C0C0C0"));
                holder.groupBadge.setText("🥈");
            } else if (position == 2) {
                holder.rankBadge.setBackgroundColor(Color.parseColor("#CD7F32"));
                holder.groupBadge.setText("🥉");
            } else {
                holder.rankBadge.setBackgroundColor(Color.parseColor("#9E9E9E"));
                holder.groupBadge.setText("🐾");
            }
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankBadge, userName, healthPoints, cryptoEquivalent, stepsText, sleepText, bpText, hrText, groupBadge;
            android.widget.ImageView profilePhoto;
            
            ViewHolder(View view) {
                super(view);
                rankBadge = view.findViewById(R.id.rankBadge);
                userName = view.findViewById(R.id.userName);
                healthPoints = view.findViewById(R.id.healthPoints);
                cryptoEquivalent = view.findViewById(R.id.cryptoEquivalent);
                stepsText = view.findViewById(R.id.stepsText);
                sleepText = view.findViewById(R.id.sleepText);
                bpText = view.findViewById(R.id.bpText);
                hrText = view.findViewById(R.id.hrText);
                groupBadge = view.findViewById(R.id.groupBadge);
                profilePhoto = view.findViewById(R.id.profilePhoto);
            }
        }
    }
}