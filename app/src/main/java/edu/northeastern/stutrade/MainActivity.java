package edu.northeastern.stutrade;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new BuyFragment());

        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        username = sessionManager.getUsername();
        email = sessionManager.getEmail();
        TextView username_tv = findViewById(R.id.username);
        username_tv.setText(username);
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
                        replaceFragment(ChatFragment.newInstance(username, email));
                        return true;
                    }
                    return true;
                }
        );



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
        createNotificationChannel();

        setupFirebaseListeners();
        if (getIntent().getStringExtra("notificationAction") != null) {
            String action = getIntent().getStringExtra("notificationAction");
            if (action.equals("openChat")) {
                String parentKey = getIntent().getStringExtra("parentKey");
                openChatFragment(parentKey);
            }
        }
    }

    private void openChatFragment(String parentKey) {
        // Create a new instance of ChatFragment with the required arguments
        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        ChatFragment chatFragment = ChatFragment.newInstance(sessionManager.getUsername(), sessionManager.getEmail(), parentKey,"true");
        MenuItem chatMenuItem = bottomNavigationView.getMenu().findItem(R.id.navigation_chat);
        chatMenuItem.setChecked(true);
        // Perform fragment transaction to open ChatFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                })
                .setNegativeButton("No", (dialog, which) -> {
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
            CharSequence name = "edu.northeastern.stutrade";
            String description = "ChannelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("edu.northeastern.stutrade", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupFirebaseListeners() {
        DatabaseReference chatFromReference = FirebaseDatabase.getInstance().getReference().child("chats")
                .child(email.substring(0, email.indexOf("@")));
        chatFromReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                    boolean hasUnreadMessage = false;

                    for (DataSnapshot childSnapshot : parentSnapshot.getChildren()) {
                        String isMessageRead = childSnapshot.child("isMessageRead").getValue(String.class);
                        if ("false".equals(isMessageRead)) {
                            hasUnreadMessage = true;
                            break;
                        }
                    }

                    if (hasUnreadMessage) {
                        String parentKey = parentSnapshot.getKey();
                        showNotificationWithIntent("New Message", "You have a new message from " + parentKey, parentKey);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void showNotificationWithIntent(String title, String message, String parentKey) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notificationAction", "openChat");
        intent.putExtra("parentKey", parentKey);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "edu.northeastern.stutrade")
                .setSmallIcon(R.drawable.stutrade_round)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        notificationManager.notify(1005, builder.build());
    }
}