package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private ImageButton adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        adminButton = findViewById(R.id.adminButton);

        // Already logged in? Skip login
        if (auth.getCurrentUser() != null) {
            goToMain();
        }

        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        adminButton.setOnClickListener(v -> {
            Toast.makeText(this, "Admin feature not yet implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    goToMain();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
