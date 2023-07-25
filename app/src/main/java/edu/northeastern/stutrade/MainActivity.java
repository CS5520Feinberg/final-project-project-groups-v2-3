package edu.northeastern.stutrade;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;

import android.os.Bundle;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new BuyFragment());

        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
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
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }
}