package edu.northeastern.stutrade;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private static final String CHANNEL_ID = "edu.northeastern.stutrade";
    private static final int NOTIFICATION_UNIQUE_ID = 1;
    public static final String EXTRA_FRAGMENT_TYPE = "fragment_type";
    public static final String MESSAGE_TO_USER = "message_to_user";
    TextView username_tv;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //replaceFragment(new BuyFragment());

        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        username_tv = findViewById(R.id.username);
        username_tv.setText(username);

        Intent intent = getIntent();
        String fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE);
        String messageToUser = intent.getStringExtra(MESSAGE_TO_USER);

        if (messageToUser != null && !messageToUser.isEmpty() && "chat_fragment".equals(fragmentType)) {
            replaceFragment(ChatFragment.newInstance(username, email, messageToUser));
        } else {
            replaceFragment(new BuyFragment());
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.navigation_sell) {
                       replaceFragment(new SellerFragment());
                        return true;
                    } else if (id == R.id.navigation_buy) {
                        replaceFragment(new BuyFragment());
                        return true;
                    } else if (id == R.id.navigation_profile) {
                        replaceFragment(ProfileFragment.newInstance(username, email));
                        return true;
                    } else if (id == R.id.navigation_chat) {
                        replaceFragment(ChatFragment.newInstance(username, email, ""));
                        return true;
                    }
                    return true;
                }
        );

        createNotificationChannel();
        String userId = email.substring(0, email.indexOf("@"));
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child("chats").child(userId);
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot fromUser : dataSnapshot.getChildren()) {
                        for (DataSnapshot messageId : fromUser.getChildren()) {
                            String isMessageSentString = messageId.child("isMessageSent").getValue(String.class);
                            boolean isMessageSent = Boolean.parseBoolean(isMessageSentString);

                            String isMessageNotifiedString = messageId.child("message_notified").getValue(String.class);
                            boolean isMessageNotified = Boolean.parseBoolean(isMessageNotifiedString);

                            if (!isMessageNotified && !isMessageSent) {
                                String message = messageId.child("message").getValue(String.class);
                                String fromUserId = String.valueOf(fromUser.getKey());
                                String fromUsername = messageId.child("name").getValue(String.class);
                                messageNotification(fromUserId, fromUsername, message);
                                chatReference.child(fromUserId).
                                        child(String.valueOf(messageId.getKey())).
                                        child("message_notified").setValue("true");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                if (currentFragment instanceof ChatFragment) {
                    ChatFragment chatFragment = (ChatFragment) currentFragment;
                    if (chatFragment.onBackPressed()) {
                        return;
                    }
                }
                if (currentFragment instanceof ProfileFragment) {
                    ProfileFragment profileFragment = (ProfileFragment) currentFragment;
                    if (profileFragment.onBackPressed()) {
                        return;
                    }
                }

                if (currentFragment instanceof ProductViewFragment) {
                    // Navigate back to BuyFragment
                    getSupportFragmentManager().popBackStack();
                }

                if (isTaskRoot()) {
                    showExitConfirmationDialog();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If the user clicks "Yes", exit the app
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If the user clicks "No", dismiss the dialog and continue with the app
                    dialog.dismiss();
                })
                .show();
    }

    private void replaceFragment(Fragment fragment) {
        fragment.setRetainInstance(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }

    void updateUsernameTextView(String newUsername) {
        username_tv.setText(newUsername);
        username = newUsername;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.RED);
            channel.enableLights(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void messageNotification(String fromUser, String fromUsername, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_FRAGMENT_TYPE, "chat_fragment");
        intent.putExtra(MainActivity.MESSAGE_TO_USER, fromUser);
        PendingIntent openIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        // Create an intent to clear the notification when the action button is clicked
        Intent clearIntent = new Intent(this, ClearNotificationHandler.class);
        clearIntent.putExtra("notificationId", NOTIFICATION_UNIQUE_ID); // Pass the notification id
        PendingIntent clearPendingIntent = PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle("New Message from " + fromUsername)
                .setContentText(message)
                .setContentIntent(openIntent)
                .setAutoCancel(true) // Remove the notification when clicked
                .addAction(R.drawable.clear_icon, "Clear", clearPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_UNIQUE_ID, builder.build());
    }
}