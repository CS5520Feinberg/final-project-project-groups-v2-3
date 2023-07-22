package edu.northeastern.stutrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

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
                        replaceFragment(new ProfileFragment());
                        return true;
                    } else if (id == R.id.navigation_chat) {
                        replaceFragment(new ChatFragment());
                        return true;
                    }
                    return true;
                }
        );
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }
}