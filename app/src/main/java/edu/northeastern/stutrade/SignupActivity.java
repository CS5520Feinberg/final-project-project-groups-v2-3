package edu.northeastern.stutrade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail, etUsername, etPassword;
    private Button btnSignup;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.email_et);
        etUsername = findViewById(R.id.name_et);
        etPassword = findViewById(R.id.password_et);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            signupUser(email, username, password);
        });
    }

    private void signupUser(String email, String username, String password) {

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Name cannot be empty");
            return;
        }

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

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Registration failed
                        Toast.makeText(SignupActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}