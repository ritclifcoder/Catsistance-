package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import java.io.File;
import com.example.myapplication.catconverter.CatVoiceAPI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.myapplication.Notification.SendNotification;
import com.example.myapplication.Notification.NotificationScheduler;
import com.example.myapplication.analytics.CloudWatchLogger;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private android.widget.TextView groupAssessmentText;
    private androidx.cardview.widget.CardView groupAssessmentCard;
    private android.os.Handler handler;
    private int messageIndex = 0;
    private java.util.List<String> messages;
    private MediaPlayer catVoicePlayer;
    private CatVoiceAPI catVoiceAPI;
    private android.view.MenuItem notificationMenuItem;
    private boolean hasUnreadNotifications = false;
    private CloudWatchLogger cloudWatchLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        loadProfilePhoto(navigationView);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                drawerLayout.closeDrawers();
                if (tabLayout.getTabCount() > 0) {
                    tabLayout.getTabAt(0).select();
                }
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_signout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_health_tracking) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, com.example.myapplication.withings.WithingsAuthActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_reminders) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_reports) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, ProfileStatisticsActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_recommendation_history) {
                drawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, RecommendationHistoryActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        tabLayout = findViewById(R.id.tabLayout);

        if (tabLayout != null) {
            tabLayout.setVisibility(android.view.View.VISIBLE);
            tabLayout.removeAllTabs();

            tabLayout.addTab(tabLayout.newTab().setText("Dashboard"));
            tabLayout.addTab(tabLayout.newTab().setText("Groups"));

            // Show first fragment by default
            loadFragment(new DashboardFragment());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new DashboardFragment();
                        groupAssessmentCard.setVisibility(android.view.View.VISIBLE);
                        break;
                    case 1:
                        fragment = new GroupFragment();
                        groupAssessmentCard.setVisibility(android.view.View.GONE);
                        break;
                }
                loadFragment(fragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Select first tab by default
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
        }

        groupAssessmentText = findViewById(R.id.groupAssessmentText);
        groupAssessmentCard = findViewById(R.id.groupAssessmentCard);
        groupAssessmentCard.setVisibility(android.view.View.VISIBLE);
        catVoiceAPI = new CatVoiceAPI(this);

        cloudWatchLogger = new CloudWatchLogger(this);
        com.example.myapplication.AmazonBedrockService.BedrockWarningEngine.setCloudWatchLogger(cloudWatchLogger);
        com.example.myapplication.WithingAPI.WithingsClient.setCloudWatchLogger(cloudWatchLogger);
        cloudWatchLogger.logEvent("MAIN_ACTIVITY_STARTED", "user=" + FirebaseAuth.getInstance().getCurrentUser().getUid());

        startRuleBasedMessageRotation();


        android.util.Log.d("MainActivity", "Calculating group vitals averages...");
        FirebaseGroupStatistics.calculateGroupAverages(this);

        saveFCMTokenAndStartNotifications();
    }

    private void startRuleBasedMessageRotation() {
        handler = new android.os.Handler();

        android.util.Log.d("MainActivity", "🔄 Starting message rotation...");
        groupAssessmentText.setText("🔄 Analyzing group dynamics...");

        if (cloudWatchLogger != null) {
            cloudWatchLogger.logEvent("MESSAGE_ROTATION_STARTED", "status=initiated");
        }

        com.example.myapplication.AmazonBedrockService.BedrockWarningEngine.reset();
        com.example.myapplication.AmazonBedrockService.BedrockWarningEngine.generateWarningMessages(generatedMessages -> {
            android.util.Log.d("MainActivity", "✅✅✅ CALLBACK RECEIVED with " + generatedMessages.size() + " messages");
            if (!generatedMessages.isEmpty()) {
                android.util.Log.d("MainActivity", "First message: " + generatedMessages.get(0));
            }
            if (cloudWatchLogger != null) {
                cloudWatchLogger.logEvent("MESSAGES_RECEIVED", "count=" + generatedMessages.size());
            }
            messages = generatedMessages;

            if (!generatedMessages.isEmpty()) {
                sendBedrockWarningNotification(generatedMessages.get(0));
            }

            runOnUiThread(() -> startRotation());
        });
    }

    private void startRotation() {
        Runnable messageRotator = new Runnable() {
            @Override
            public void run() {
                if (groupAssessmentText != null && messages != null && !messages.isEmpty()) {
                    String currentWarning = messages.get(messageIndex);
                    groupAssessmentText.setText(currentWarning);
                    if (cloudWatchLogger != null) {
                        cloudWatchLogger.logEvent("WARNING_DISPLAYED", "index=" + messageIndex);
                    }

                    // Send notification every 2 rotations (about 2 per minute)
                    if (messageIndex % 2 == 0) {
                        sendBedrockWarningNotification(currentWarning);
                    }

                    playCatVoiceForMessage();
                    messageIndex = (messageIndex + 1) % messages.size();

                    // Regenerate messages every 10 rotations for fresh content
                    if (messageIndex == 0) {
                        com.example.myapplication.AmazonBedrockService.BedrockWarningEngine.generateWarningMessages(newMessages -> {
                            messages = newMessages;
                        });
                    }

                    handler.postDelayed(this, 19000);
                }
            }
        };
        handler.post(messageRotator);
    }

    private void playCatVoiceForMessage() {
        if (groupAssessmentText == null || messages == null) return;

        String currentMessage = messages.get(messageIndex);

        catVoiceAPI.textToCatSpeech(currentMessage, new CatVoiceAPI.ConversionCallback() {
            @Override
            public void onSuccess(File catVoiceFile) {
                if (cloudWatchLogger != null) {
                    cloudWatchLogger.logEvent("CAT_VOICE_PLAYED", "file=" + catVoiceFile.getName());
                }
                runOnUiThread(() -> {
                    try {
                        if (catVoicePlayer != null) {
                            catVoicePlayer.release();
                        }

                        catVoicePlayer = new MediaPlayer();
                        catVoicePlayer.setDataSource(catVoiceFile.getAbsolutePath());
                        catVoicePlayer.prepare();
                        catVoicePlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MainActivity", "Cat TTS error: " + error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (catVoicePlayer != null) {
            catVoicePlayer.release();
        }
        NotificationScheduler.cancelGroupAlerts();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        notificationMenuItem = menu.findItem(R.id.action_notifications);
        checkForUnreadNotifications();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chat) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_cat_converter) {
            Intent intent = new Intent(this, com.example.myapplication.catconverter.MainActivityCat.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_notifications) {
            hasUnreadNotifications = false;
            updateNotificationIcon();
            Intent intent = new Intent(this, ReminderActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileStatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_test_notification) {
            testNotification();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void sendBedrockWarningNotification(String warningMessage) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Save to Firebase notifications node
        String notificationId = FirebaseDatabase.getInstance().getReference().push().getKey();
        java.util.Map<String, Object> notificationData = new java.util.HashMap<>();
        notificationData.put("title", "🚨 Group Performance Alert");
        notificationData.put("message", warningMessage);
        notificationData.put("timestamp", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("notifications")
            .child(userId)
            .child(notificationId)
            .setValue(notificationData);

        // Send local notification
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "bedrock_alerts";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                channelId,
                "Bedrock Health Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            notificationManager.createNotificationChannel(channel);
        }

        android.content.Intent intent = new Intent(this, ReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_cat_notification)
            .setContentTitle("🚨 Group Performance Alert")
            .setContentText(warningMessage)
            .setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(warningMessage))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        android.util.Log.d("MainActivity", "✅ Bedrock notification sent");
    }

    private void testNotification() {
        android.util.Log.d("MainActivity", "🔔 Testing notification...");

        // Test local notification first
        testLocalNotification();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .child("fcmToken")
            .get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String token = snapshot.getValue(String.class);
                    android.util.Log.d("MainActivity", "✅ FCM Token from DB: " + token);

                    SendNotification sendNotification = new SendNotification(
                        token,
                        "🐱 Catsistance Test",
                        "Meow! Your notifications are working!",
                        this
                    );

                    sendNotification.SendNotifications();
                    android.widget.Toast.makeText(this, "✅ Sending test notification...", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("MainActivity", "❌ No FCM token in database");
                    android.widget.Toast.makeText(this, "No FCM token found. Restart app.", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("MainActivity", "❌ Failed to get token: " + e.getMessage());
                android.widget.Toast.makeText(this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            });
    }

    private void testLocalNotification() {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "test_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                channelId,
                "Test Channel",
                android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            notificationManager.createNotificationChannel(channel);
        }

        android.content.Intent intent = new Intent(this, ReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_cat_notification)
            .setContentTitle("🐱 Local Test")
            .setContentText("This is a local notification test!")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(pendingIntent);

        notificationManager.notify(999, builder.build());
        android.util.Log.d("MainActivity", "✅ Local notification sent");
    }

    private void checkForUnreadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("notifications")
            .child(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        hasUnreadNotifications = true;
                        updateNotificationIcon();
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {}
            });
    }

    private void updateNotificationIcon() {
        if (notificationMenuItem != null) {
            if (hasUnreadNotifications) {
                notificationMenuItem.getIcon().setTint(getResources().getColor(R.color.primary_green));
            } else {
                notificationMenuItem.getIcon().setTintList(null);
            }
        }
    }

    private void loadProfilePhoto(NavigationView navigationView) {
        android.view.View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = headerView.findViewById(R.id.navHeaderProfileImage);
        android.widget.TextView usernameText = headerView.findViewById(R.id.navHeaderUsername);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://englishdic-80c3a.firebaseio.com/").getReference("Users")
            .child(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && usernameText != null) {
                            usernameText.setText(name);
                        }

                        String photoUri = snapshot.child("photoUri").getValue(String.class);
                        if (photoUri != null && photoUri.startsWith("https://")) {
                            new Thread(() -> {
                                try {
                                    java.net.URL url = new java.net.URL(photoUri);
                                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                    runOnUiThread(() -> profileImage.setImageBitmap(bitmap));
                                } catch (Exception e) {
                                    runOnUiThread(() -> profileImage.setImageResource(R.drawable.catpuccino));
                                }
                            }).start();
                        } else {
                            profileImage.setImageResource(R.drawable.catpuccino);
                        }
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {}
            });
    }

    private void saveFCMTokenAndStartNotifications() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .child("fcmToken")
                        .setValue(token);

                    NotificationScheduler.scheduleGroupAlerts(this);
                    android.util.Log.d("MainActivity", "FCM token saved and notifications scheduled");
                }
            });
    }
}