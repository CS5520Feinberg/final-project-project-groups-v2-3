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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //replaceFragment(new BuyFragment());

        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        TextView username_tv = findViewById(R.id.username);
        username_tv.setText(username);

        Intent intent = getIntent();
        String fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE);
        String messageToUser = intent.getStringExtra(MESSAGE_TO_USER);

        if (messageToUser != null && !messageToUser.isEmpty() && "chat_fragment".equals(fragmentType)) {
            replaceFragment(ChatFragment.newInstance(username, email, messageToUser));
        } else {
            // If no specific fragment type provided, replace with BuyFragment (default)
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
        DatabaseReference chatToReference = FirebaseDatabase.getInstance().getReference().child("chats").child(userId);
        chatToReference.addValueEventListener(new ValueEventListener() {
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
                                String messageFromUser = String.valueOf(fromUser.getKey());
                                messageNotification(messageFromUser, message);
                                chatToReference.child(messageFromUser).
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

                // Check if the current fragment is a ChatFragment
                if (currentFragment instanceof ChatFragment) {
                    ChatFragment chatFragment = (ChatFragment) currentFragment;
                    if (chatFragment.onBackPressed()) {
                        // The back button press was handled by the ChatFragment, do nothing further.
                        return;
                    }
                }
                if (currentFragment instanceof ProfileFragment) {
                    ProfileFragment profileFragment = (ProfileFragment) currentFragment;
                    if (profileFragment.onBackPressed()) {
                        // The back button press was handled by the ChatFragment, do nothing further.
                        return;
                    }
                }

                if (currentFragment instanceof ProductViewFragment) {
                    // Navigate back to BuyFragment
                    getSupportFragmentManager().popBackStack();
                }

                // Check if the current activity is the root activity
                if (isTaskRoot()) {
                    // If it is the root activity, call finish to exit the app
                   // finishAffinity();
                    showExitConfirmationDialog();
                } else {
                    // If it's not the root activity, propagate the back press event to the activity stack
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
        TextView username_tv = findViewById(R.id.username);
        username_tv.setText(newUsername);
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

    private void messageNotification(String fromUser, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_FRAGMENT_TYPE, "chat_fragment");
        intent.putExtra(MainActivity.MESSAGE_TO_USER, fromUser);
        PendingIntent openIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        // Create an intent to clear the notification when the action button is clicked
        Intent clearIntent = new Intent(this, ClearNotificationHandler.class);
        clearIntent.putExtra("notificationId", NOTIFICATION_UNIQUE_ID); // Pass the notification id
        PendingIntent clearPendingIntent = PendingIntent.getBroadcast(this, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle("New Message from " + fromUser)
                .setContentText(message)
                .setContentIntent(openIntent)
                .setAutoCancel(true) // Remove the notification when clicked
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_UNIQUE_ID, builder.build());
    }
}