package edu.northeastern.stutrade;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private SharedPreferences preferences;

    public UserSessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserDetails(String username, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }
}

