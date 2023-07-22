package edu.northeastern.stutrade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        Button login = findViewById(R.id.login_button);
//        login.setOnClickListener(view -> {
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//        });

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.username_edit_text);
        etPassword = findViewById(R.id.password_edit_text);
        btnLogin = findViewById(R.id.login_button);
        btnSignup = findViewById(R.id.signup_button);
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            loginUser(email, password);
        });

        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUser(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email cannot be empty");
            return;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email format");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password cannot be empty");
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        UserSessionManager sessionManager = new UserSessionManager(getApplicationContext());
                        sessionManager.saveUserDetails("JohnDoe", "johndoe@example.com");

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}